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
        coverageReadsCurrentMemoryWithoutChangingIt();
        auditRepairsOnlyFlaggedRows();
        templatesKeepSubjectsAndValidateValues();
        migratesOnlyOldValueTemplates();
        groupsPossessivesAndAdjacentValues();
        migratesOnlyDamagedSlotStructures();
        literalPossessivesKeepTermsAndContractions();
        migratesLiteralNounsAndExportsEveryPendingRow();
        separatesAdjacentEscapeCodesFromDialogue();
        preservesSpeakersAndLocalizesWholeLabels();
        countsPreservedEntriesSeparately();
        rejectsSuperficialAndDamagedReplies();
        System.out.println("19 JVM translation checks passed");
    }

    private static void separatesAdjacentEscapeCodesFromDialogue() throws Exception {
        String source = "GUIDE: Thank you!\\nI'll wait here.\\GPlay?";
        java.util.List<String> tokens = new java.util.ArrayList<>();
        equal("GUIDE: Thank you!⟦0⟧I'll wait here.⟦1⟧Play?", TranslationBatch.shield(source, tokens));
        equal(Arrays.asList("\\n", "\\G"), tokens);
        equal(Arrays.asList("\\n[1]", "\\Custom[2]", "\\name"), TranslationBatch.tokens("\\n[1]\\Custom[2]\\name"));
        equal(true, TranslationBatch.isSafe(source, "GUIDE: ¡Gracias!\\nEsperaré aquí.\\G¿Jugar?"));
        equal(false, TranslationBatch.isSafe(source, "GUIDE: ¡Gracias! Esperaré aquí. ¿Jugar?"));
    }

    private static void preservesSpeakersAndLocalizesWholeLabels() throws Exception {
        java.util.List<String> sources = Arrays.asList("ELENA: Um...", "GRUNT: ...", "Bubble", "Sp. Atk: {1}", "HP {1}/{2}");
        Map<String, String> fixed = TranslationAudit.translate(sources, (raw, input) -> { throw new AssertionError("Unnecessary inference"); });
        equal("ELENA: Eh...", fixed.get(sources.get(0)));
        equal("RECLUTA: ...", fixed.get(sources.get(1)));
        equal("Burbuja", fixed.get("Bubble"));
        equal("At. Esp.: {1}", fixed.get("Sp. Atk: {1}"));
        equal("PS {1}/{2}", fixed.get("HP {1}/{2}"));
        equal(null, TranslationLabels.fixed("I found a Pound coin.", TranslationProfile.EMPTY));
        equal(null, TranslationLabels.fixed("Pound", TranslationProfile.EMPTY));
        equal(null, TranslationLabels.fixed("Return", TranslationProfile.EMPTY));
        equal(true, NeuralText.needsNounRepair("{1}'s Return missed!"));
        String text = "ELENA: Wait here.";
        Map<String, String> answers = TranslationAudit.translate(Collections.singletonList(text),
            (raw, input) -> NeuralText.translate(raw, input, plain -> {
                equal(Collections.singletonList("Wait here."), plain);
                return Collections.singletonList("Espera aquí.");
            }, html -> null));
        equal("ELENA: Espera aquí.", answers.get(text));
        equal(TranslationAudit.Issue.NAMES, TranslationAudit.check(text, "HELENA: Espera aquí."));
        equal("please wait", TranslationDialogue.normalize("plz wait"));
        equal(true, TranslationDialogue.normalizes("C’mon, it is time to go."));
        equal("ELENA: Qué va.", TranslationLabels.fixed("ELENA: Nu-uh.", TranslationProfile.EMPTY));
        equal("ELENA: Tachán!", TranslationLabels.fixed("ELENA: Ta-daaa!", TranslationProfile.EMPTY));
        equal("T3RR4: there are no forfeits in MY domain", TranslationDialogue.normalize("T3RR4: TH3R3 4R3 N0 F0RF3ITS 1N MY D0M41N"));
        equal("The password is 31337", TranslationDialogue.normalize("The password is 31337"));
        equal(false, TranslationProfile.EMPTY.preserve("\\ch[1,Yes,No]"));
        equal(false, TranslationProfile.EMPTY.preserve("<user's cry>"));
        equal(false, TranslationProfile.EMPTY.preserve("[English]"));
        equal(true, TranslationProfile.EMPTY.preserve("<ac>{1:03d}</ac>"));
    }

    private static void countsPreservedEntriesSeparately() throws Exception {
        Path dir = Files.createTempDirectory("gahvoria-classification");
        Path catalog = dir.resolve("catalog.ready"), cache = dir.resolve("es.tsv");
        try {
            java.util.List<String> sources = Arrays.asList("ELENA: ...", "ELENA: Hm.", "Elena", "{1:03d} / {2:03d}",
                "Pokedex/pokedexbg", "HP: {1}", "Please wait", "Mystery English", "Unknown Person");
            Files.write(catalog, sources, StandardCharsets.UTF_8);
            Files.write(cache, Arrays.asList("Please wait\tEspera", "HP: {1}\tPV: {1}"), StandardCharsets.UTF_8);
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                equal("Elena", memory.get("Elena"));
                equal(null, memory.get("Unknown Person"));
                equal(null, memory.get("HP: {1}"));
                equal("Espera", memory.get("Please wait"));
                memory.remember(TranslationAudit.translate(Collections.singletonList("HP: {1}"), (a,b) -> null, memory.profile));
            }
            TranslationCoverage report = TranslationCoverage.read(dir.resolve("catalog").toFile());
            equal(9, report.total); equal(4, report.preserved); equal(1, report.internal);
            equal(2, report.translated); equal(2, report.pending);
            java.io.StringWriter export = new java.io.StringWriter();
            equal(2, TranslationCoverage.writePending(dir.resolve("catalog").toFile(), export));
            equal(false, export.toString().contains("\tElena\t"));
            equal(true, export.toString().contains("\tUnknown Person\t"));
            try (TranslationMemory memory = TranslationMemory.snapshot(cache.toFile())) {
                equal("PS: {1}", memory.get("HP: {1}"));
            }
        } finally { for (Path path : Arrays.asList(cache, catalog, dir)) Files.deleteIfExists(path); }
    }

    private static void rejectsSuperficialAndDamagedReplies() {
        equal(TranslationAudit.Issue.UNCHANGED, TranslationAudit.check("Unknown", "¡Unknown!"));
        equal(TranslationAudit.Issue.UNCHANGED, TranslationAudit.check("Unknown", "UNKNOWN"));
        equal(TranslationAudit.Issue.REVIEW, TranslationAudit.check("Garrr!", "¡¡¡!!!"));
        equal(TranslationAudit.Issue.REVIEW, TranslationAudit.check("N.x. d.m..g.d t.xt.", "¡N.x. d.m..g.d text!"));
        equal(TranslationAudit.Issue.REVIEW, TranslationAudit.check("Z9C0RRUPT arrived", "Z9C0RRUPT llegó"));
        equal(null, TranslationAudit.check("PP: {1}/{2}", "PP: {1}/{2}"));
        equal(null, TranslationAudit.check("GRUNT: ...", "RECLUTA: ..."));
        equal(TranslationAudit.Issue.NAMES, TranslationAudit.check("Anna, Brian Smith, Carla, David, Elena, Frank",
            "Ana, Brian Smith, Carla, David, Elena, Frank"));
        equal(false, TranslationDialogue.possibleCreditList("bread, milk, cheese, water, rice, eggs"));
    }

    private static void literalPossessivesKeepTermsAndContractions() throws Exception {
        String source = "{1}’s Rain Dish restored its HP a little!";
        Map<String, String> answers = TranslationAudit.translate(Collections.singletonList(source),
            (raw, text) -> NeuralText.translate(raw, text, input -> {
                equal(Collections.singletonList("ZXQVA restored its HP a little!"), input);
                return Collections.singletonList("ZXQVA recuperó algunos PS.");
            }, input -> null));
        equal("Cura Lluvia de {1} recuperó algunos PS.", answers.get(source));
        String dreams = "{1}'s dreams jolted them right back up!";
        answers = TranslationBatch.translate(Collections.singletonList(dreams),
            (raw, text) -> NeuralText.translate(raw, text, input -> {
                equal(Collections.singletonList("the dreams of ZXQVA jolted them right back up!"), input);
                return Collections.singletonList("¡Los sueños de ZXQVA lo despertaron!");
            }, input -> null));
        equal("¡Los sueños de {1} lo despertaron!", answers.get(dreams));
        for (String untouched : Arrays.asList("{1}'s been waiting.", "{1}'s going home.", "{1}'s {2:03d} years old.",
            "{1}'s healthiness improved.", "{1}'s family’s house.", "{1}'s Rain Dishware broke.", "{1}'s \\c[2]Rain Dish activated.",
            "Run Away from {1}.", "Tell {1} about Cloud Nine.")) equal(false, NeuralText.needsNounRepair(untouched));
        String controls = "\\c[1]{1}'s Rain Dish\\c[0] activated!";
        answers = TranslationBatch.translate(Collections.singletonList(controls),
            (raw, text) -> NeuralText.translate(raw, text, input -> null, input -> {
                equal(Collections.singletonList("<img data-gahvoria=\"0\">ZXQVB<img data-gahvoria=\"2\"> activated!"), input);
                return Collections.singletonList("<img data-gahvoria=\"0\">ZXQVB<img data-gahvoria=\"2\"> se activó!");
            }));
        equal("\\c[1]Cura Lluvia de {1}\\c[0] se activó!", answers.get(controls));
        for (String output : Arrays.asList("ZXQVA ZXQVA", "ZXQVAó", "Sin valor")) {
            equal(0, TranslationBatch.translate(Collections.singletonList(source),
                (raw, text) -> NeuralText.translate(raw, text, input -> Collections.singletonList(output), input -> null)).size());
        }
    }

    private static void migratesLiteralNounsAndExportsEveryPendingRow() throws Exception {
        Path folder = Files.createTempDirectory("gahvoria-noun-report");
        Path cache = folder.resolve("es.tsv"), ready = folder.resolve("catalog.ready");
        String source = "{1}'s Rain Dish restored its HP a little!";
        String bad = "{1}'s Rain Dish restaura sus PS.";
        String dreams = "{1}'s dreams jolted them right back up!";
        try {
            Files.write(cache, Arrays.asList("Hello\tHola", "\u001Fslots-v1:" + source + "\t" + bad,
                "\u001Fgroups-v1:" + dreams + "\tLos sueños de {1} los sacudieron.",
                "\u001Fgroups-v1:{1}'s {2} blocks {3}!\t¡{2} de {1} bloquea {3}!"), StandardCharsets.UTF_8);
            java.util.List<String> rows = new java.util.ArrayList<>(Arrays.asList("Hello", source, dreams, source));
            for (int i = 0; i < 30; i++) rows.add("Pending " + i + "\\nline\\tvalue");
            Files.write(ready, rows, StandardCharsets.UTF_8);
            long size = Files.size(cache);
            TranslationCoverage before = TranslationCoverage.read(folder.resolve("catalog").toFile());
            equal(33, before.total);
            equal(1, before.translated);
            equal(12, before.examples.size());
            java.io.StringWriter export = new java.io.StringWriter();
            equal(32, TranslationCoverage.writePending(folder.resolve("catalog").toFile(), export));
            equal(true, export.toString().contains("ENGLISH\t" + source + "\t" + bad));
            equal(true, export.toString().contains("OUTDATED\t" + dreams));
            equal(true, export.toString().contains("Pending 29\\nline\\tvalue"));
            equal(size, Files.size(cache));
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                equal(null, memory.get(dreams));
                equal("¡{2} de {1} bloquea {3}!", memory.get("{1}'s {2} blocks {3}!"));
                memory.remember(Collections.singletonMap(source, "Cura Lluvia de {1} recuperó algunos PS."));
                memory.remember(Collections.singletonMap(dreams, "Los sueños de {1} lo despertaron."));
            }
            try (TranslationMemory reader = new TranslationMemory(cache.toFile())) {
                equal("Cura Lluvia de {1} recuperó algunos PS.", reader.get(source));
                equal("Hola", reader.get("Hello"));
            }
            TranslationCoverage after = TranslationCoverage.read(folder.resolve("catalog").toFile());
            equal(3, after.translated);
            equal(30, TranslationCoverage.writePending(folder.resolve("catalog").toFile(), new java.io.StringWriter()));
        } finally {
            for (Path path : Arrays.asList(cache, ready, folder)) Files.deleteIfExists(path);
        }
    }

    private static void groupsPossessivesAndAdjacentValues() throws Exception {
        String source = "{1}'s {2} {3}boosted its {4}!";
        String translated = "¡{2} de {1} {3} aumentó su {4}!";
        Map<String, String> result = TranslationBatch.translate(Collections.singletonList(source),
            (raw, protectedText) -> NeuralText.translate(raw, protectedText, input -> {
                equal(Collections.singletonList("ZXQVA ZXQVC boosted its ZXQVD!"), input);
                return Collections.singletonList("¡ZXQVA ZXQVC aumentó su ZXQVD!");
            }, input -> { throw new AssertionError("No literal controls in this template"); }));
        equal(translated, result.get(source));
        equal(null, TranslationAudit.check(source, translated));
        equal(TranslationAudit.Issue.ENGLISH, TranslationAudit.check("{1}'s {2} is ready.", "{1}'s {2} está listo."));
        equal(true, NeuralText.needsSlotRepair(source));
        equal(true, NeuralText.needsSlotRepair("{1}boosted morale."));
        equal(false, NeuralText.needsSlotRepair("What will {1} do?"));
        equal(false, NeuralText.needsSlotRepair("{1}'s {2:03d} years old."));
        String joined = "The password is {1}{2}.";
        result = TranslationBatch.translate(Collections.singletonList(joined),
            (raw, protectedText) -> NeuralText.translate(raw, protectedText, input -> {
                equal(Collections.singletonList("The password is ZXQVA."), input);
                return Collections.singletonList("La contraseña es ZXQVA.");
            }, input -> null));
        equal("La contraseña es {1}{2}.", result.get(joined));
        // Neither a pause nor a formatting region may be pulled inside the noun phrase.
        String colored = "\\c[1]{1}'s {2}\\c[0] blocks {3}!";
        result = TranslationBatch.translate(Collections.singletonList(colored),
            (raw, protectedText) -> NeuralText.translate(raw, protectedText, input -> null, input -> {
                equal(Collections.singletonList("<img data-gahvoria=\"0\">ZXQVB<img data-gahvoria=\"3\"> blocks ZXQVE!"), input);
                return Collections.singletonList("<img data-gahvoria=\"0\">ZXQVB<img data-gahvoria=\"3\"> bloquea ZXQVE!");
            }));
        equal("\\c[1]{2} de {1}\\c[0] bloquea {3}!", result.get(colored));
        equal(false, NeuralText.needsSlotRepair("{1}'s \\c[2]{2}"));
        // The model must not duplicate, mutate or silently lose a grouped value.
        for (String output : Arrays.asList("ZXQVA ZXQVA", "ZXQVAó", "Sin valor", "ZXQVB")) {
            equal(0, TranslationBatch.translate(Collections.singletonList("{1}'s {2}"),
                (raw, protectedText) -> NeuralText.translate(raw, protectedText,
                    input -> Collections.singletonList(output), input -> null)).size());
        }
    }

    private static void migratesOnlyDamagedSlotStructures() throws Exception {
        Path cache = Files.createTempFile("gahvoria-groups", ".tsv");
        String source = "{1}'s {2} blocks {3}!";
        String replacement = "¡{2} de {1} bloquea {3}!";
        Files.write(cache, Arrays.asList("Hello\tHola",
            "\u001Fslots-v1:What will {1} do?\t¿Qué hará {1}?",
            "\u001Fslots-v1:" + source + "\t¡{1} {2} bloquea {3}!"), StandardCharsets.UTF_8);
        try {
            try (TranslationMemory memory = TranslationMemory.snapshot(cache.toFile())) {
                equal(TranslationAudit.Issue.OUTDATED, memory.audit(source));
                equal(null, memory.get(source));
                equal("¿Qué hará {1}?", memory.get("What will {1} do?"));
                equal("Hola", memory.get("Hello"));
            }
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                memory.remember(Collections.singletonMap(source, replacement));
            }
            try (TranslationMemory memory = TranslationMemory.snapshot(cache.toFile())) {
                equal(replacement, memory.get(source));
                equal(null, memory.audit(source));
            }
            try (TranslationMemory memory = new TranslationMemory(cache.toFile())) {
                equal(replacement, memory.get(source));
            }
        } finally { Files.delete(cache); }
    }

    private static void templatesKeepSubjectsAndValidateValues() throws Exception {
        String source = "What will {1} do?";
        Map<String, String> result = TranslationBatch.translate(Collections.singletonList(source),
            (raw, inputs) -> NeuralText.translate(raw, inputs, sentences -> {
                equal(Collections.singletonList("What will ZXQVA do?"), sentences);
                return Collections.singletonList("¿Qué hará ZXQVA?");
            }, markup -> { throw new AssertionError("No markup needed for values"); }));
        equal("¿Qué hará {1}?", result.get(source));
        // Named values may move within one formatting region; controls cannot.
        equal(true, TranslationBatch.isSafe("{1}'s {2}", "{2} de {1}"));
        equal(false, TranslationBatch.isSafe("{1} \\c[1]{2}", "{2} \\c[1]{1}"));
        equal(false, TranslationBatch.isSafe("{1:03d}", "{1:d}"));
        equal(false, TranslationBatch.isSafe("{1} {1}", "{1} {2}"));
        equal(false, TranslationBatch.isSafe("\\c[1]Hi\\c[2]", "\\c[2]Hola\\c[1]"));
        equal("{2} de {1}", TranslationBatch.translate(Collections.singletonList("{1}'s {2}"),
            (raw, inputs) -> Collections.singletonList("⟦1⟧ de ⟦0⟧")).get("{1}'s {2}"));
        for (String broken : Arrays.asList("¿Qué hará ZXQVB?", "¿Qué hará ZXQVA ZXQVA?",
                                           "¿Qué hará ZXQ VA?", "¿Qué hará?")) {
            equal(0, TranslationBatch.translate(Collections.singletonList(source),
                (raw, inputs) -> NeuralText.translate(raw, inputs,
                    sentences -> Collections.singletonList(broken), sentences -> null)).size());
        }
        String colored = "\\c[1]Hello, {1}!";
        result = TranslationBatch.translate(Collections.singletonList(colored),
            (raw, inputs) -> NeuralText.translate(raw, inputs, sentences -> {
                throw new AssertionError("Keep complete message through alignment");
            }, sentences -> {
                equal(Collections.singletonList("<img data-gahvoria=\"0\">Hello, ZXQVB!"), sentences);
                return Collections.singletonList("<img data-gahvoria=\"0\">¡Hola, ZXQVB!");
            }));
        equal("\\c[1]¡Hola, {1}!", result.get(colored));
        equal(true, NeuralText.usesValueSlots("Hello \\PN"));
        equal(true, NeuralText.usesValueSlots("Money {2:03d}"));
        equal(false, NeuralText.usesValueSlots("\\c[1]Ready!\\se[Sound]"));
        equal(false, NeuralText.usesValueSlots("Hello %s"));
    }

    private static void migratesOnlyOldValueTemplates() throws Exception {
        Path cache = Files.createTempFile("gahvoria-slots", ".tsv");
        String source = "Welcome, {1}!";
        Files.write(cache, Arrays.asList("Hello\tHola",
            "\u001Fcontext-v1:" + source + "\tBienvenido{1}.",
            "\u001Fcontext-v1:\\\\c[1]Ready\t\\\\c[1]Listo"), StandardCharsets.UTF_8);
        try {
            try (TranslationMemory memory = TranslationMemory.snapshot(cache.toFile())) {
                equal(TranslationAudit.Issue.OUTDATED, memory.audit(source));
                equal("Hola", memory.get("Hello"));
                equal("\\c[1]Listo", memory.get("\\c[1]Ready"));
            }
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                memory.remember(Collections.singletonMap(source, "¡Bienvenido, {1}!"));
            }
            try (TranslationMemory memory = TranslationMemory.snapshot(cache.toFile())) {
                equal("¡Bienvenido, {1}!", memory.get(source));
            }
        } finally { Files.delete(cache); }
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
                equal(Collections.singletonList("<img data-gahvoria=\"0\">You are an <img data-gahvoria=\"1\">E"
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

        equal(Arrays.asList("Porque, ya sabes", "Cos Island"), NeuralText.translate(
            Arrays.asList("Cuz, you know", "Cos Island"), inputs -> {
                equal(Arrays.asList("Because, you know", "Cos Island"), inputs);
                return Arrays.asList("Porque, ya sabes", "Cos Island");
            }, utterances -> { throw new AssertionError("Plain lines"); }));
        equal(Collections.singletonList("Tenemos tus pases."), NeuralText.translate(
            Collections.singletonList("We’ve got your passes."), inputs -> {
                equal(Collections.singletonList("We have got your passes."), inputs);
                return Collections.singletonList("Tenemos tus pases.");
            }, utterances -> { throw new AssertionError("Plain lines"); }));
    }

    private static void refreshesOnlyOldContextSensitiveEntries() throws Exception {
        File cache = File.createTempFile("gahvoria-context", ".tsv");
        try {
            try (TranslationMemory legacy = new TranslationMemory(cache)) {
                legacy.remember(Collections.singletonMap("Hi {1}", "Hola {1}"));
                legacy.remember(Collections.singletonMap("One", "Uno"));
                legacy.remember(Collections.singletonMap("Cuz, look.", "Cuz, mira."));
            }
            try (TranslationMemory current = new TranslationMemory(cache, true)) {
                equal("Uno", current.get("One"));
                equal(null, current.get("Hi {1}"));
                equal(null, current.get("Cuz, look."));
                current.remember(Collections.singletonMap("Hi {1}", "Qué tal, {1}"));
                current.remember(Collections.singletonMap("Cuz, look.", "Porque, mira."));
            }
            try (TranslationMemory reopened = new TranslationMemory(cache)) {
                equal("Qué tal, {1}", reopened.get("Hi {1}"));
                equal("Uno", reopened.get("One"));
                equal("Porque, mira.", reopened.get("Cuz, look."));
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
        Path opening = folder.resolve(TranslationOpening.FILE_NAME);
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

    private static void coverageReadsCurrentMemoryWithoutChangingIt() throws Exception {
        Path folder = Files.createTempDirectory("gahvoria-coverage");
        Path catalog = folder.resolve("catalog");
        Path ready = folder.resolve("catalog.ready");
        Path limited = folder.resolve("catalog.limited");
        Path cache = folder.resolve("es.tsv");
        try {
            Files.write(ready, "Hello\nMissing\\nline\nHello\nCuz, look.\nOther game\n".getBytes(StandardCharsets.UTF_8));
            TranslationCoverage empty = TranslationCoverage.read(catalog.toFile());
            equal(4, empty.total);
            equal(0, empty.translated);
            equal(false, Files.exists(cache));
            Files.write(cache, "Hello\tHola\nCuz, look.\tCuz, mira.\nUnlisted\tOtro\n".getBytes(StandardCharsets.UTF_8));
            Files.createFile(limited);
            try (TranslationMemory writer = new TranslationMemory(cache.toFile(), true)) {
                TranslationCoverage before = TranslationCoverage.read(catalog.toFile());
                equal(4, before.total);
                equal(1, before.translated);
                equal(true, before.partial);
                equal(Arrays.asList("Missing line", "Cuz, look.", "Other game"), before.examples);
                writer.remember(Collections.singletonMap("Cuz, look.", "Porque, mira."));
                try (RandomAccessFile tail = new RandomAccessFile(cache.toFile(), "rw")) {
                    tail.seek(tail.length());
                    tail.write("Other game\tOtro juego".getBytes(StandardCharsets.UTF_8));
                }
                long size = Files.size(cache);
                TranslationCoverage after = TranslationCoverage.read(catalog.toFile());
                equal(2, after.translated);
                equal(Arrays.asList("Missing line", "Other game"), after.examples);
                equal(size, Files.size(cache));
            }
        } finally {
            for (Path path : Arrays.asList(ready, limited, cache, folder)) Files.deleteIfExists(path);
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        }
    }

    private static void auditRepairsOnlyFlaggedRows() throws Exception {
        Path folder = Files.createTempDirectory("gahvoria-audit");
        Path catalog = folder.resolve("catalog");
        Path ready = folder.resolve("catalog.ready");
        Path cache = folder.resolve("es.tsv");
        Path opening = folder.resolve(TranslationOpening.FILE_NAME);
        String english = "We've got your passes.";
        String formatted = "Hello {1}.";
        String unchanged = "See you soon.";
        String term = "Pokémon Center";
        try {
            Files.write(ready, Arrays.asList("One", english, formatted, unchanged, term, "Missing"), StandardCharsets.UTF_8);
            Files.write(cache, Arrays.asList("One\tUno", english + "\tWe've conseguido tus pases.",
                "\u001Fcontext-v1:" + formatted + "\tHola.",
                unchanged + "\tSee you soon!", term + "\tCentro de Pokemones"), StandardCharsets.UTF_8);
            TranslationCoverage before = TranslationCoverage.read(catalog.toFile());
            equal(6, before.total);
            equal(1, before.translated);
            for (TranslationAudit.Issue issue : Arrays.asList(TranslationAudit.Issue.FORMAT,
                    TranslationAudit.Issue.ENGLISH, TranslationAudit.Issue.UNCHANGED,
                    TranslationAudit.Issue.TERMINOLOGY, TranslationAudit.Issue.MISSING)) {
                equal(1, before.issues.get(issue));
            }
            try (TranslationMemory memory = new TranslationMemory(cache.toFile(), true)) {
                java.util.List<String> pending = new java.util.ArrayList<>();
                for (String source : Files.readAllLines(ready, StandardCharsets.UTF_8)) {
                    if (memory.get(source) == null) pending.add(source);
                }
                memory.remember(TranslationAudit.translate(pending, (originals, inputs) -> {
                    equal(Arrays.asList(english, formatted, unchanged, "Missing"), originals);
                    return Arrays.asList("Tenemos tus pases.", "Hola ⟦0⟧.", "Hasta pronto.", "Missing");
                }));
                equal("Uno", memory.get("One"));
                equal("Centro Pokémon", memory.get(term));
                equal(null, memory.get("Missing"));
                TranslationOpening.publish(ready.toFile(), memory);
            }
            equal(Arrays.asList("One\tUno", english + "\tTenemos tus pases.", formatted + "\tHola {1}.",
                unchanged + "\tHasta pronto.", term + "\tCentro Pokémon"), Files.readAllLines(opening, StandardCharsets.UTF_8));
            TranslationCoverage after = TranslationCoverage.read(catalog.toFile());
            equal(5, after.translated);
            equal(Collections.singletonMap(TranslationAudit.Issue.MISSING, 1), after.issues);
            equal(TranslationAudit.Issue.FORMAT, TranslationAudit.check("Go!\u0001", "¡Ve!"));
            equal(null, TranslationAudit.check("We'll meet at Lavender Town.", "Nos vemos en Lavender Town."));
            equal(null, TranslationAudit.check("Cos Island", "Isla Cos"));
        } finally {
            for (Path path : Arrays.asList(ready, cache, opening, folder)) Files.deleteIfExists(path);
        }
    }
}
