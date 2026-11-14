package com.hatkid.mkxpz.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read the prepared catalog and the complete rows of its Spanish memory. */
public final class TranslationCoverage {
    private static final int MAX_EXAMPLES = 12;
    private static final int MAX_EXAMPLE_LENGTH = 180;

    public final int total;
    public final int translated;
    public final boolean partial;
    public final List<String> examples;
    public final Map<TranslationAudit.Issue, Integer> issues;

    private TranslationCoverage(int total, int translated, boolean partial, List<String> examples,
                                Map<TranslationAudit.Issue, Integer> issues) {
        this.total = total;
        this.translated = translated;
        this.partial = partial;
        this.examples = Collections.unmodifiableList(examples);
        this.issues = Collections.unmodifiableMap(issues);
    }

    public static TranslationCoverage read(File catalogBase) throws IOException {
        File catalog = new File(catalogBase.getPath() + ".ready");
        File memoryFile = new File(catalogBase.getParentFile(), "es.tsv");
        int translated = 0;
        Set<String> originals = new HashSet<>();
        List<String> examples = new ArrayList<>();
        Map<TranslationAudit.Issue, Integer> issues = new EnumMap<>(TranslationAudit.Issue.class);
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
                TranslationAudit.Issue issue = memory == null ? TranslationAudit.Issue.MISSING : memory.audit(original);
                if (issue == null) {
                    translated++;
                } else {
                    Integer count = issues.get(issue);
                    issues.put(issue, count == null ? 1 : count + 1);
                    if (examples.size() < MAX_EXAMPLES) {
                        String snippet = original.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
                        int limit = snippet.offsetByCodePoints(0, Math.min(
                            snippet.codePointCount(0, snippet.length()), MAX_EXAMPLE_LENGTH));
                        examples.add(limit < snippet.length() ? snippet.substring(0, limit) + "…" : snippet);
                    }
                }
            }
        }
        return new TranslationCoverage(originals.size(), translated, partial, examples, issues);
    }
}
