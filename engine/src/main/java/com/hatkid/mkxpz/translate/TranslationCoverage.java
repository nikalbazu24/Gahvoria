package com.hatkid.mkxpz.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
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
    public final int preserved;
    public final int internal;
    public final int pending;
    public final boolean partial;
    public final List<String> examples;
    public final Map<TranslationAudit.Issue, Integer> issues;

    private TranslationCoverage(int total, int translated, int preserved, int internal, boolean partial, List<String> examples,
                                Map<TranslationAudit.Issue, Integer> issues) {
        this.total = total;
        this.translated = translated;
        this.preserved = preserved;
        this.internal = internal;
        this.pending = total - translated - preserved - internal;
        this.partial = partial;
        this.examples = Collections.unmodifiableList(examples);
        this.issues = Collections.unmodifiableMap(issues);
    }

    public static TranslationCoverage read(File catalogBase) throws IOException {
        File catalog = new File(catalogBase.getPath() + ".ready");
        File memoryFile = new File(catalogBase.getParentFile(), "es.tsv");
        int translated = 0, preserved = 0, internal = 0;
        Set<String> originals = new HashSet<>();
        List<String> examples = new ArrayList<>();
        Map<TranslationAudit.Issue, Integer> issues = new EnumMap<>(TranslationAudit.Issue.class);
        boolean partial = new File(catalogBase.getPath() + ".limited").isFile();
        try (BufferedReader rows = new BufferedReader(
                 new InputStreamReader(new FileInputStream(catalog), StandardCharsets.UTF_8.newDecoder()));
             TranslationMemory memory = memoryFile.isFile() ? TranslationMemory.snapshot(memoryFile) : null) {
            TranslationProfile profile = memory == null ? TranslationProfile.read(catalog) : memory.profile;
            String row;
            while ((row = rows.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("Revisión interrumpida");
                String original = TranslationMemory.unescape(row);
                if (original == null || original.trim().isEmpty()) {
                    partial = true;
                    continue;
                }
                if (!originals.add(original)) continue;
                if (TranslationProfile.internal(original)) { internal++; continue; }
                if (profile.preserve(original)) { preserved++; continue; }
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
        return new TranslationCoverage(originals.size(), translated, preserved, internal, partial, examples, issues);
    }

    /** Export every pending row, not just the twelve UI examples. Stream the
     * report so large catalogs are not copied into the Android clipboard. */
    public static int writePending(File catalogBase, Writer output) throws IOException {
        File catalog = new File(catalogBase.getPath() + ".ready");
        File memoryFile = new File(catalogBase.getParentFile(), "es.tsv");
        Set<String> seen = new HashSet<>();
        int count = 0, preserved = 0, internal = 0;
        output.write("# Gahvoria: pendientes de traducción\n");
        output.write("# Campos TSV escapados: \\n, \\r, \\t y \\\\. Una fila por original.\n");
        output.write("# Sin traducción guardada no permite distinguir rechazo del modelo de una frase aún no procesada.\n");
        output.write("motivo\toriginal\ttraduccion_guardada\n");
        try (BufferedReader rows = new BufferedReader(new InputStreamReader(
                 new FileInputStream(catalog), StandardCharsets.UTF_8.newDecoder()));
             TranslationMemory memory = memoryFile.isFile() ? TranslationMemory.snapshot(memoryFile) : null) {
            TranslationProfile profile = memory == null ? TranslationProfile.read(catalog) : memory.profile;
            String row;
            while ((row = rows.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("Exportación interrumpida");
                String original = TranslationMemory.unescape(row);
                if (original == null || original.trim().isEmpty() || !seen.add(original)) continue;
                if (TranslationProfile.internal(original)) { internal++; continue; }
                if (profile.preserve(original)) { preserved++; continue; }
                TranslationAudit.Issue issue = memory == null ? TranslationAudit.Issue.MISSING : memory.audit(original);
                if (issue == null) continue;
                output.write(issue.name());
                output.write('\t');
                output.write(TranslationMemory.escape(original));
                output.write('\t');
                output.write(TranslationMemory.escape(memory == null ? "" : memory.storedCandidate(original)));
                output.write('\n');
                count++;
            }
        }
        output.write("# Conservadas sin traducir: " + preserved + " · Referencias internas: " + internal + "\n");
        output.write("# Pendientes exportados: " + count + "\n");
        output.flush();
        return count;
    }
}
