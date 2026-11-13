package com.hatkid.mkxpz.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** A bounded copy of prepared translations, readable before the game's worker responds. */
final class TranslationOpening {
    private TranslationOpening() {}

    static void publish(File catalog, TranslationMemory memory) throws IOException {
        File target = new File(catalog.getParentFile(), "opening-es.txt");
        File temp = new File(target.getPath() + ".tmp");
        try {
            Map<String, String> ready = new LinkedHashMap<>();
            int chars = 0;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(catalog), StandardCharsets.UTF_8))) {
                for (int i = 0; i < 768; i++) {
                    String row = input.readLine();
                    if (row == null) break;
                    String source = TranslationMemory.unescape(row);
                    if (source == null || source.isEmpty()) continue;
                    String answer = memory.get(source);
                    if (answer == null || ready.containsKey(source)) continue;
                    chars += source.length() + answer.length();
                    if (chars > 96_000) break;
                    ready.put(source, answer);
                }
            }
            try (RandomAccessFile output = new RandomAccessFile(temp, "rw")) {
                output.setLength(0);
                TranslationMemory.append(output, ready, true);
            }
            if (!temp.renameTo(target)) throw new IOException("No pude guardar los textos de inicio");
        } finally {
            temp.delete();
        }
    }
}
