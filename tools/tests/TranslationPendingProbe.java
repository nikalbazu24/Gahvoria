package com.hatkid.mkxpz.translate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Local-only replay of an exported pending report. Never commit its input/output. */
public final class TranslationPendingProbe {
    public static void main(String[] args) throws Exception {
        Set<String> sources = new LinkedHashSet<>();
        Map<String, String> stored = new LinkedHashMap<>();
        for (String row : Files.readAllLines(Paths.get(args[4]), StandardCharsets.UTF_8)) {
            if (row.startsWith("#") || row.startsWith("motivo\t") || row.isEmpty()) continue;
            String[] cells = row.split("\t", -1);
            if (cells.length != 3) throw new IOException("Invalid report row");
            String original = TranslationMemory.unescape(cells[1]);
            if (original == null) throw new IOException("Invalid escaped original");
            sources.add(original);
            String candidate = TranslationMemory.unescape(cells[2]);
            if (candidate == null) throw new IOException("Invalid escaped candidate");
            if (!candidate.isEmpty()) stored.put(original, candidate);
        }
        Path dir = Files.createTempDirectory("gahvoria-pending-");
        try {
            List<String> rows = new ArrayList<>();
            for (String original : sources) rows.add(TranslationMemory.escape(original));
            Files.write(dir.resolve("catalog.ready"), rows, StandardCharsets.UTF_8);
            TranslationProfile profile = TranslationProfile.read(dir.resolve("catalog.ready").toFile());
            NeuralText.Inference plain = t -> NeuralInferenceProbe.infer(args, false, t);
            NeuralText.Inference html = t -> NeuralInferenceProbe.infer(args, true, t);
            // Audit-only replay measures stored candidates separately from fresh inference.
            // The exported report has no cache-version keys, so this is not a phone migration simulation.
            Map<String, String> answers = args.length > 5 && "--audit-stored".equals(args[5]) ? stored
                : TranslationAudit.translate(new ArrayList<>(sources), (o,t) -> NeuralText.translate(o,t,plain,html), profile);
            try (TranslationMemory memory = new TranslationMemory(dir.resolve("es.tsv").toFile(), true)) {
                memory.remember(answers);
            }
            try (TranslationMemory memory = TranslationMemory.snapshot(dir.resolve("es.tsv").toFile())) {
                for (String original : sources) {
                    String answer = memory.get(original);
                    String status = TranslationProfile.internal(original) ? "INTERNAL" : profile.preserve(original)
                        ? "PRESERVED" : answer != null ? "TRANSLATED" : "PENDING";
                    System.out.println(enc(original) + "\t" + enc(answer) + "\t" + status
                        + "\t" + enc(answers.get(original)));
                }
            }
            TranslationCoverage coverage = TranslationCoverage.read(dir.resolve("catalog").toFile());
            StringWriter exported = new StringWriter();
            int pending = TranslationCoverage.writePending(dir.resolve("catalog").toFile(), exported);
            if (pending != coverage.pending || coverage.total != sources.size()) throw new AssertionError("Coverage mismatch");
            System.err.println("Total " + coverage.total + "; translated " + coverage.translated
                + "; preserved " + coverage.preserved + "; internal " + coverage.internal + "; pending " + coverage.pending);
        } finally {
            try (java.util.stream.Stream<Path> files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException e) { throw new UncheckedIOException(e); } });
            }
        }
    }
    private static String enc(String text) {
        return text == null ? "-" : Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
