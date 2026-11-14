package com.hatkid.mkxpz.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Read the prepared catalog and the complete rows of its Spanish memory. */
public final class TranslationCoverage {
    private static final int MAX_EXAMPLES = 12;
    private static final int MAX_EXAMPLE_LENGTH = 180;

    public final int total;
    public final int translated;
    public final boolean partial;
    public final List<String> examples;

    private TranslationCoverage(int total, int translated, boolean partial, List<String> examples) {
        this.total = total;
        this.translated = translated;
        this.partial = partial;
        this.examples = Collections.unmodifiableList(examples);
    }

    public static TranslationCoverage read(File catalogBase) throws IOException {
        File catalog = new File(catalogBase.getPath() + ".ready");
        File memoryFile = new File(catalogBase.getParentFile(), "es.tsv");
        int translated = 0;
        Set<String> originals = new HashSet<>();
        List<String> examples = new ArrayList<>();
        boolean partial = new File(catalogBase.getPath() + ".limited").isFile();
        try (BufferedReader rows = new BufferedReader(
                 new InputStreamReader(new FileInputStream(catalog), StandardCharsets.UTF_8.newDecoder()));
             TranslationMemory memory = memoryFile.isFile() ? TranslationMemory.snapshot(memoryFile) : null) {
            String row;
            while ((row = rows.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("Revisión interrumpida");
                String original = TranslationMemory.unescape(row);
                if (original == null || original.trim().isEmpty()) {
                    partial = true;
                    continue;
                }
                if (!originals.add(original)) continue;
                if (memory != null && memory.get(original) != null) {
                    translated++;
                } else if (examples.size() < MAX_EXAMPLES) {
                    String snippet = original.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
                    int limit = snippet.offsetByCodePoints(0, Math.min(
                        snippet.codePointCount(0, snippet.length()), MAX_EXAMPLE_LENGTH));
                    examples.add(limit < snippet.length() ? snippet.substring(0, limit) + "…" : snippet);
                }
            }
        }
        return new TranslationCoverage(originals.size(), translated, partial, examples);
    }
}
