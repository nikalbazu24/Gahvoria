package com.hatkid.mkxpz.translate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Optional real-model probe. Uses the exact app pipeline and a local Node/WASM worker. */
public final class NeuralInferenceProbe {
    public static void main(String[] args) throws Exception {
        List<String> sources = new ArrayList<>(Arrays.asList(
            "What will {1} do?", "Welcome, {1}!", "You found {1}!",
            "Please follow {1}.", "The gate opens after {1} badges.",
            "Give {1} to {2}.", "{1} is waiting at the station.",
            "Are you ready, {1}?", "The ferry leaves in {1} minutes.",
            "You need {1} coins to enter.", "The library is closed until {1}.",
            "{1} can go ahead of me; I do not mind.",
            "If {1} is ready, we can leave.", "I have a gift for {1}.",
            "The bridge to {1} has been repaired.", "Would you like to save your progress, {1}?",
            "There are {1} rooms left.", "Do not forget to talk to {1}.",
            "You cannot enter without {1}.", "We will meet {1} after the battle.",
            "You received {1} coins from {2}.", "{1} will join you on this journey.",
            "Your next opponent is {1}.", "{1} won the tournament!",
            "Can you help {1} find the missing key?", "The shop in {1} sells medicine.",
            "{1} has returned safely.", "Take {1} to {2}, then return here.",
            "\\c[1]Welcome, {1}!", "You have {1:03d} points.",
            "The score is {1}/{2}.", "Welcome, \\PN! We have been waiting for you."
        ));
        if (args.length > 4) sources.addAll(java.nio.file.Files.readAllLines(
            java.nio.file.Paths.get(args[4]), StandardCharsets.UTF_8));
        NeuralText.Inference plain = texts -> infer(args, false, texts);
        NeuralText.Inference aligned = texts -> infer(args, true, texts);
        Map<String,String> before = TranslationBatch.translate(sources,
            (raw,texts) -> NeuralText.translate(texts,plain,aligned));
        Map<String,String> after = TranslationBatch.translate(sources,
            (raw,texts) -> NeuralText.translate(raw,texts,plain,aligned));
        for (String source : sources) {
            System.out.println(encode(source) + "\t" + encode(before.get(source)) + "\t" + encode(after.get(source)));
        }
        System.err.println("Valid template inventories: " + before.size() + "/" + sources.size()
            + " before; " + after.size() + "/" + sources.size() + " after. This is not a fluency score.");
        if (!"¿Qué hará {1}?".equals(after.get(sources.get(0)))) throw new AssertionError("Lost grammatical subject");
        for (String source : sources) {
            if (!after.containsKey(source)) throw new AssertionError("Missing template: " + source);
            String answer = after.get(source);
            if (answer.matches(".*\\}[’']s.*")) throw new AssertionError("Retained English possessive: " + answer);
        }
        if (after.size() < before.size()) throw new AssertionError("Template preservation regressed");
    }
    private static String encode(String s) {
        return s == null ? "-" : Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
    static List<String> infer(String[] args, boolean html, List<String> texts) throws IOException {
        if (texts.isEmpty()) return Collections.emptyList();
        Process p = new ProcessBuilder(args[0], args[1], args[2], args[3], html ? "html" : "plain")
            .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try (Writer out = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8)) {
            for (String text : texts) out.write(encode(text) + "\n");
        }
        List<String> output = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) output.add(new String(Base64.getDecoder().decode(line), StandardCharsets.UTF_8));
        }
        try { if (p.waitFor() != 0) throw new IOException("Model worker failed"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException(e); }
        return output;
    }
}
