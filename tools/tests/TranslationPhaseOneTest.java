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
        System.out.println("3 JVM translation checks passed");
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

    private static void equal(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        }
    }
}
