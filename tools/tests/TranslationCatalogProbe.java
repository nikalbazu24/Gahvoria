package com.hatkid.mkxpz.translate;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Real-model integration probe: inference, audit, persistence, reopen, coverage,
 * opening delivery and complete pending export. Synthetic ability combinations
 * exercise grammar; they are not a list of real ability effects or a game score. */
public final class TranslationCatalogProbe {
    public static void main(String[] args) throws Exception {
        LinkedHashSet<String> unique = new LinkedHashSet<>(Files.readAllLines(Paths.get(args[4]), StandardCharsets.UTF_8));
        for (String[] term : NeuralTerms.ABILITIES) {
            unique.add("{1}'s " + term[0] + " restored its HP a little!");
            unique.add("{1}'s " + term[0] + " raised its {2}!");
            unique.add("The effect of {1}'s " + term[0] + " wore off!");
        }
        List<String> sources = new ArrayList<>(unique);
        NeuralText.Inference plain = texts -> NeuralInferenceProbe.infer(args, false, texts);
        NeuralText.Inference aligned = texts -> NeuralInferenceProbe.infer(args, true, texts);
        Map<String, String> answers = TranslationAudit.translate(sources,
            (raw, texts) -> NeuralText.translate(raw, texts, plain, aligned));
        Path dir = Files.createTempDirectory("gahvoria-model-catalog");
        Path catalog = dir.resolve("catalog.ready"), cache = dir.resolve("es.tsv");
        List<String> escaped = new ArrayList<>();
        for (String source : sources) escaped.add(TranslationMemory.escape(source));
        Files.write(catalog, escaped, StandardCharsets.UTF_8);
        int accepted = 0;
        try {
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                memory.remember(answers);
                TranslationOpening.publish(catalog.toFile(), memory);
            }
            try (TranslationMemory reopened = TranslationMemory.snapshot(cache.toFile())) {
                for (String source : sources) {
                    String answer = reopened.get(source);
                    if (answer != null) accepted++;
                    System.out.println(TranslationMemory.escape(source) + "\t"
                        + TranslationMemory.escape(answers.getOrDefault(source, "")) + "\t"
                        + (reopened.audit(source) == null ? "OK" : reopened.audit(source)));
                    if (answer != null && (args.length < 6 || !"--baseline".equals(args[5]))) {
                        for (String[] term : NeuralTerms.ABILITIES) {
                            if (source.contains("'s " + term[0] + " ") && !answer.contains(term[1])) {
                                throw new AssertionError("Lost ability term: " + source + " => " + answer);
                            }
                        }
                    }
                }
            }
            TranslationCoverage report = TranslationCoverage.read(dir.resolve("catalog").toFile());
            if (report.translated != accepted || report.total != sources.size()) throw new AssertionError("Coverage disagrees with persistence");
            java.io.StringWriter export = new java.io.StringWriter();
            if (TranslationCoverage.writePending(dir.resolve("catalog").toFile(), export) != sources.size() - accepted) {
                throw new AssertionError("Pending export count disagrees");
            }
            if (accepted == sources.size() && Files.readAllLines(dir.resolve(TranslationOpening.FILE_NAME)).size()
                    != Math.min(TranslationOpening.MAX_ROWS, sources.size())) throw new AssertionError("Opening delivery incomplete");
            System.err.println("Audited, saved and reopened: " + accepted + "/" + sources.size()
                + "; pending export: " + (sources.size() - accepted) + ". Synthetic corpus; not full-game quality.");
            if (args.length < 6 || !"--baseline".equals(args[5])) {
                if (accepted != sources.size()) throw new AssertionError("Some catalog rows remain rejected");
            }
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(dir)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) Files.deleteIfExists(path);
            }
        }
    }
}
