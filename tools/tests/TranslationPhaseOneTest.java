package com.hatkid.mkxpz.translate;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public final class TranslationPhaseOneTest {
    public static void main(String[] args) throws Exception {
        preservesControlTokens();
        rejectsBadProviderOutput();
        persistsOnlyValidatedTranslations();
        neuralPreservesMarkupWithoutSendingItToInference();
        neuralKeepsOneUtteranceAcrossControls();
        refreshesOnlyOldContextSensitiveEntries();
        openingUsesPreparedEntriesAndReplacesStaleRows();
        System.out.println("7 JVM translation checks passed");
    }

    private static void preservesControlTokens() throws Exception {
        String original = "\\c[1]Hello \\PN!\nGo <b>now</b>, {1} %s.";
        String expected = "\\c[1]¡Hola, \\PN!\nVe <b>ahora</b>, {1} %s.";
        Map<String, String> translated = TranslationBatch.translate(Collections.singletonList(original), (raw, inputs) -> {
            equal(original, raw.get(0));
            equal("⟦0⟧Hello ⟦1⟧!⟦2⟧Go ⟦3⟧now⟦4⟧, ⟦5⟧ ⟦6⟧.", inputs.get(0));
            return Collections.singletonList("⟦0⟧¡Hola, ⟦1⟧!⟦2⟧Ve ⟦3⟧ahora⟦4⟧, ⟦5⟧ ⟦6⟧.");
        });
        equal(expected, translated.get(original));
        equal(false, TranslationBatch.isSafe("Total {1:02d}", "Total {1:d}"));
    }

    private static void neuralPreservesMarkupWithoutSendingItToInference() throws Exception {
        String original = "\\c[1]Welcome \\PN!\nGet <b>ready</b>.";
        Map<String, String> result = TranslationBatch.translate(Collections.singletonList(original),
            (raw, inputs) -> NeuralText.translate(inputs, spans -> {
                equal(Arrays.asList("Welcome", "Get", "ready"), spans);
                return Arrays.asList("Bienvenido", "Prepárate", "ya");
            }));
        equal("\\c[1]Bienvenido \\PN!\nPrepárate <b>ya</b>.", result.get(original));
        equal(Collections.singletonList(null),
            NeuralText.translate(Collections.singletonList("Hello ⟦0⟧!"), spans -> Collections.singletonList(null)));
        equal(Arrays.asList(null, "⟦0⟧ ¡Vamos!", "Final"),
            NeuralText.translate(Arrays.asList("Hello ⟦0⟧ again", "⟦0⟧ Go!", "End"), spans -> {
                equal(Arrays.asList("Hello", "again", "Go!", "End"), spans);
                return Arrays.asList(null, "otra vez", "¡Vamos!", "Final");
            }));
        equal(Collections.singletonList("⟦0⟧!"),
            NeuralText.translate(Collections.singletonList("⟦0⟧!"), spans -> { throw new AssertionError("No text"); }));
    }

    private static void neuralKeepsOneUtteranceAcrossControls() throws Exception {
        String source = "\\c[1]You're an \\c[2]E\\c[1]-rank Trainer, {1}!";
        String expected = "\\c[1]Eres un entrenador de rango \\c[2]E\\c[1], {1}!";
        Map<String, String> result = TranslationBatch.translate(Collections.singletonList(source),
            (raw, inputs) -> NeuralText.translate(inputs, fragments -> {
                throw new AssertionError("The whole utterance should fit in one input");
            }, utterances -> {
                equal(Collections.singletonList("<img data-gahvoria=\"0\">You're an <img data-gahvoria=\"1\">E"
                    + "<img data-gahvoria=\"2\">-rank Trainer, <img data-gahvoria=\"3\">!"), utterances);
                return Collections.singletonList("<img data-gahvoria=\"0\">Eres un entrenador de rango "
                    + "<img data-gahvoria=\"1\">E<img data-gahvoria=\"2\">, <img data-gahvoria=\"3\">!");
            }));
        equal(expected, result.get(source));

        equal(Collections.singletonList("⟦0⟧Adiós"), NeuralText.translate(
            Collections.singletonList("⟦0⟧Bye"), inputs -> Collections.singletonList("Adiós"),
            utterances -> Collections.singletonList("Adiós<img data-gahvoria=\"0\">")));
        equal(Collections.singletonList("⟦0⟧Hola ⟦1⟧"), NeuralText.translate(
            Collections.singletonList("⟦0⟧Hello ⟦1⟧"), inputs -> Collections.singletonList("Hola"),
            utterances -> Collections.singletonList("<img data-gahvoria=\"1\">Hola<img data-gahvoria=\"0\">")));
        equal(Collections.singletonList("Pijama"), NeuralText.translate(Collections.singletonList("Jammies"),
            inputs -> {
                equal(Collections.singletonList("pajamas"), inputs);
                return Collections.singletonList("Pijama");
            }, utterances -> { throw new AssertionError("Plain line"); }));
    }

    private static void refreshesOnlyOldContextSensitiveEntries() throws Exception {
        File cache = File.createTempFile("gahvoria-context", ".tsv");
        try {
            try (TranslationMemory legacy = new TranslationMemory(cache)) {
                legacy.remember(Collections.singletonMap("Hi {1}", "Hola {1}"));
                legacy.remember(Collections.singletonMap("One", "Uno"));
            }
            try (TranslationMemory current = new TranslationMemory(cache, true)) {
                equal("Uno", current.get("One"));
                equal(null, current.get("Hi {1}"));
                current.remember(Collections.singletonMap("Hi {1}", "Qué tal, {1}"));
            }
            try (TranslationMemory reopened = new TranslationMemory(cache)) {
                equal("Qué tal, {1}", reopened.get("Hi {1}"));
                equal("Uno", reopened.get("One"));
            }
        } finally {
            Files.delete(cache.toPath());
        }
    }

    private static void rejectsBadProviderOutput() throws Exception {
        for (String broken : Arrays.asList("⟦0⟧hola", "⟦1⟧hola⟦0⟧", "⟦0⟧⟦0⟧hola⟦1⟧",
            "⟦0⟧hola⟦1⟧\\v[1]", "⟦0⟧hola⟦1⟧\n", "", null)) {
            Map<String, String> translated = TranslationBatch.translate(Collections.singletonList("\\c[1]Hi\\PN"),
                (raw, inputs) -> Collections.singletonList(broken));
            equal(0, translated.size());
        }
        try {
            TranslationBatch.translate(Collections.singletonList("hello"), (raw, inputs) -> Collections.emptyList());
            throw new AssertionError("Expected mismatched provider response to fail");
        } catch (IOException expected) {
            equal("El proveedor devolvió otra cantidad de frases", expected.getMessage());
        }
    }

    private static void persistsOnlyValidatedTranslations() throws Exception {
        Path folder = Files.createTempDirectory("gahvoria-memory-test");
        File cache = folder.resolve("es.tsv").toFile();
        File otherGame = folder.resolve("other-game.tsv").toFile();
        File otherLanguage = folder.resolve("pt.tsv").toFile();
        File save = folder.resolve("Game.rxdata").toFile();
        try {
            byte[] originalSave = {9, 12, 4};
            Files.write(save.toPath(), originalSave);
            Files.write(cache.toPath(), ("Hello\tHola\nSame\tSame\n\\c\tUnsafe\n"
                + "Invalid\\z\tMal\nBusted\tToo\tmany\nBad\\q\tMal\ncut\tincomplete").getBytes(StandardCharsets.UTF_8));
            try (TranslationMemory memory = new TranslationMemory(cache);
                 TranslationMemory other = new TranslationMemory(otherGame);
                 TranslationMemory portuguese = new TranslationMemory(otherLanguage)) {
                equal("Hola", memory.get("Hello"));
                equal(null, other.get("Hello"));
                equal(null, portuguese.get("Hello"));
                for (String invalid : Arrays.asList("Same", "\\c", "Invalidz", "Busted", "Badq", "cut")) {
                    equal(null, memory.get(invalid));
                }
                memory.remember(Collections.singletonMap("Hello", "Buenas"));
                memory.remember(Collections.singletonMap("No change", "No change"));
                memory.remember(Collections.singletonMap("\\c[1]Wait", "Espera"));
                memory.remember(Collections.singletonMap("A\\PN\tline\n", "Una\\PN\tlínea\n"));
                equal("Hola", memory.get("Hello"));
                equal(null, memory.get("No change"));
                equal(null, memory.get("\\c[1]Wait"));
            }
            try (RandomAccessFile failedWrite = new RandomAccessFile(cache, "r")) {
                long size = failedWrite.length();
                try {
                    TranslationMemory.append(failedWrite, Collections.singletonMap("Lost", "Perdido"), true);
                    throw new AssertionError("Expected read-only file to reject writes");
                } catch (IOException expected) {
                    equal(size, failedWrite.length());
                }
            }
            try (TranslationMemory reopened = new TranslationMemory(cache)) {
                equal("Una\\PN\tlínea\n", reopened.get("A\\PN\tline\n"));
                equal(null, reopened.get("cut"));
                equal(null, reopened.get("Lost"));
            }
            if (!Arrays.equals(originalSave, Files.readAllBytes(save.toPath()))) throw new AssertionError("Save changed");
        } finally {
            for (File file : new File[]{cache, otherGame, otherLanguage, save}) Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(folder);
        }
    }

    private static void openingUsesPreparedEntriesAndReplacesStaleRows() throws Exception {
        Path folder = Files.createTempDirectory("gahvoria-opening");
        Path catalog = folder.resolve("catalog.ready");
        Path cache = folder.resolve("es.tsv");
        Path opening = folder.resolve("opening-es.txt");
        try {
            Files.write(catalog, "First\nUnknown\nFirst\nChoose\\twell\n".getBytes(StandardCharsets.UTF_8));
            Files.write(opening, "Old\tViejo\n".getBytes(StandardCharsets.UTF_8));
            try (TranslationMemory memory = new TranslationMemory(cache.toFile())) {
                memory.remember(Collections.singletonMap("First", "Primero"));
                memory.remember(Collections.singletonMap("Choose\twell", "Elige\tbien"));
                memory.remember(Collections.singletonMap("Unlisted", "Fuera del catálogo"));
                TranslationOpening.publish(catalog.toFile(), memory);
                equal(Arrays.asList("First\tPrimero", "Choose\\twell\tElige\\tbien"),
                    Files.readAllLines(opening, StandardCharsets.UTF_8));
                Files.write(catalog, "Unknown\n".getBytes(StandardCharsets.UTF_8));
                TranslationOpening.publish(catalog.toFile(), memory);
                equal(0L, Files.size(opening));
            }
        } finally {
            for (Path path : Arrays.asList(catalog, cache, opening, folder)) Files.deleteIfExists(path);
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        }
    }
}
