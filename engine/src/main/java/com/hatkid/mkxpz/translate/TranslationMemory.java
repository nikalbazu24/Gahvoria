package com.hatkid.mkxpz.translate;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    private final Map<String, TranslationAudit.Issue> rejected;

    TranslationMemory(File path) throws IOException {
        this(path, false);
    }

    TranslationMemory(File path, boolean contextual) throws IOException {
        this(path, contextual, true);
    }

    static TranslationMemory snapshot(File path) throws IOException {
        return new TranslationMemory(path, true, false);
    }

    private TranslationMemory(File path, boolean contextual, boolean writable) throws IOException {
        this.contextual = contextual;
        rejected = writable ? null : new HashMap<>();
        file = new RandomAccessFile(path, writable ? "rw" : "r");
        try {
            lock = writable ? file.getChannel().tryLock() : null;
            if (writable && lock == null) throw new IOException("La memoria de traducción está en uso");
            load(writable, file.length());
        } catch (IOException | RuntimeException failure) {
            file.close();
            throw failure;
        }
    }

    String get(String original) {
        return entries.get(key(original));
    }

    private String key(String original) {
        String connector = CONNECTOR_PREFIX + original;
        if (entries.containsKey(connector) || (contextual && NeuralText.hasColloquialConnector(original))) return connector;
        String revised = CONTEXT_PREFIX + original;
        if (entries.containsKey(revised) || (contextual && TranslationBatch.needsContext(original))) return revised;
        return original;
    }

    TranslationAudit.Issue audit(String original) {
        String key = key(original);
        if (entries.containsKey(key)) return null;
        TranslationAudit.Issue issue = rejected == null ? null : rejected.get(key);
        if (issue == null && !key.equals(original)) {
            issue = rejected == null ? null : rejected.get(original);
            if (issue == null && entries.containsKey(original)) return TranslationAudit.Issue.OUTDATED;
        }
        return issue == null ? TranslationAudit.Issue.MISSING : issue;
    }

    void remember(Map<String, String> answers) throws IOException {
        Map<String, String> fresh = new LinkedHashMap<>();
        for (Map.Entry<String, String> answer : answers.entrySet()) {
            String original = answer.getKey();
            String translated = answer.getValue();
            if (original.isEmpty() || get(original) != null) continue;
            if (contextual ? TranslationAudit.check(original, translated) != null
                           : original.equals(translated) || !TranslationBatch.isSafe(original, translated)) continue;
            String key = key(original);
            if (row(key, translated).getBytes(StandardCharsets.UTF_8).length <= MAX_ROW_BYTES) {
                fresh.put(key, translated);
            }
        }
        if (fresh.isEmpty()) return;
        append(file, fresh, true);
        entries.putAll(fresh);
    }

    private void load(boolean writable, long length) throws IOException {
        byte[] block = new byte[8192];
        ByteArrayOutputStream row = new ByteArrayOutputStream();
        long offset = 0;
        long complete = 0;
        int count;
        while (offset < length && (count = file.read(block, 0, (int) Math.min(block.length, length - offset))) != -1) {
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
        if (writable && complete != offset) {
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
        if (!contextual) {
            if (!source.equals(translated) && TranslationBatch.isSafe(source, translated)) entries.put(original, translated);
            return;
        }
        TranslationAudit.Issue issue = TranslationAudit.check(source, translated);
        if (issue == null) {
            entries.put(original, translated);
        } else if (rejected != null && !entries.containsKey(original)) {
            rejected.put(original, issue);
        }
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
