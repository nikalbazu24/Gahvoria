package com.hatkid.mkxpz.translate;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Per-game, per-target UTF-8 TSV. The :game process owns the single writer. */
final class TranslationMemory implements Closeable {
    private static final int MAX_ROW_BYTES = 64 * 1024;
    private static final String CONTEXT_PREFIX = "\u001Fcontext-v1:";
    private static final String CONNECTOR_PREFIX = "\u001Fconnector-v1:";
    private final RandomAccessFile file;
    private final boolean contextual;
    private final java.nio.channels.FileLock lock;
    private final Map<String, String> entries = new LinkedHashMap<>();

    TranslationMemory(File path) throws IOException {
        this(path, false);
    }

    TranslationMemory(File path, boolean contextual) throws IOException {
        this.contextual = contextual;
        file = new RandomAccessFile(path, "rw");
        try {
            lock = file.getChannel().tryLock();
            if (lock == null) throw new IOException("La memoria de traducción está en uso");
            load();
        } catch (IOException | RuntimeException failure) {
            file.close();
            throw failure;
        }
    }

    String get(String original) {
        String connector = entries.get(CONNECTOR_PREFIX + original);
        if (connector != null || (contextual && NeuralText.hasColloquialConnector(original))) return connector;
        String revised = entries.get(CONTEXT_PREFIX + original);
        if (revised != null) return revised;
        return contextual && TranslationBatch.needsContext(original) ? null : entries.get(original);
    }

    void remember(Map<String, String> answers) throws IOException {
        Map<String, String> fresh = new LinkedHashMap<>();
        for (Map.Entry<String, String> answer : answers.entrySet()) {
            String original = answer.getKey();
            String translated = answer.getValue();
            if (original.isEmpty() || original.equals(translated) || get(original) != null
                || !TranslationBatch.isSafe(original, translated)) continue;
            String key = original;
            if (contextual && NeuralText.hasColloquialConnector(original)) key = CONNECTOR_PREFIX + original;
            else if (contextual && TranslationBatch.needsContext(original)) key = CONTEXT_PREFIX + original;
            if (row(key, translated).getBytes(StandardCharsets.UTF_8).length <= MAX_ROW_BYTES) {
                fresh.put(key, translated);
            }
        }
        if (fresh.isEmpty()) return;
        append(file, fresh, true);
        entries.putAll(fresh);
    }

    private void load() throws IOException {
        byte[] block = new byte[8192];
        ByteArrayOutputStream row = new ByteArrayOutputStream();
        long offset = 0;
        long complete = 0;
        int count;
        while ((count = file.read(block)) != -1) {
            for (int i = 0; i < count; i++) {
                if (block[i] == '\n') {
                    if (row.size() <= MAX_ROW_BYTES) readRow(row.toByteArray());
                    row.reset();
                    complete = offset + i + 1;
                } else if (row.size() <= MAX_ROW_BYTES) {
                    row.write(block[i]);
                }
            }
            offset += count;
        }
        // A killed process may leave an incomplete last row. Drop it before appending.
        if (complete != offset) {
            file.setLength(complete);
            file.getFD().sync();
        }
    }

    private void readRow(byte[] bytes) {
        final String line;
        try {
            line = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException badRow) {
            return;
        }
        int tab = line.indexOf('\t');
        if (tab <= 0 || line.indexOf('\t', tab + 1) >= 0) return;
        int end = line.endsWith("\r") ? line.length() - 1 : line.length();
        String original = unescape(line.substring(0, tab));
        String translated = unescape(line.substring(tab + 1, end));
        if (original == null) return;
        String source = original.startsWith(CONTEXT_PREFIX) ? original.substring(CONTEXT_PREFIX.length()) : original;
        if (original.startsWith(CONNECTOR_PREFIX)) source = original.substring(CONNECTOR_PREFIX.length());
        if (!source.equals(translated) && TranslationBatch.isSafe(source, translated)) entries.put(original, translated);
    }

    static void append(RandomAccessFile file, Map<String, String> answers, boolean durable) throws IOException {
        if (answers.isEmpty()) return;
        StringBuilder batch = new StringBuilder();
        for (Map.Entry<String, String> answer : answers.entrySet()) {
            batch.append(row(answer.getKey(), answer.getValue()));
        }
        long start = file.length();
        file.seek(start);
        try {
            file.write(batch.toString().getBytes(StandardCharsets.UTF_8));
            if (durable) file.getFD().sync();
        } catch (IOException failure) {
            try {
                file.setLength(start);
            } catch (IOException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
    }

    private static String row(String original, String translated) {
        return escape(original) + '\t' + escape(translated) + '\n';
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
    }

    static String unescape(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\') {
                result.append(c);
                continue;
            }
            if (++i == value.length()) return null;
            switch (value.charAt(i)) {
                case '\\': result.append('\\'); break;
                case 'n': result.append('\n'); break;
                case 't': result.append('\t'); break;
                case 'r': result.append('\r'); break;
                default: return null;
            }
        }
        return result.toString();
    }

    @Override public void close() throws IOException { file.close(); }
}
