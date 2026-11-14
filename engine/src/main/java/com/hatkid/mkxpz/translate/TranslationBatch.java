package com.hatkid.mkxpz.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared boundary for the current providers and a future on-device model. */
final class TranslationBatch {
    private static final Pattern CODES = Pattern.compile(
        "\\\\[A-Za-z]+(?:\\[[^\\]\\r\\n]*\\])?|\\\\[.!|^<>{}$\\\\]|"
        + "<[^>\\r\\n]{1,40}>|\\[[A-Za-z_]+\\]|\\{\\d+(?::[A-Za-z0-9. +#-]+)?\\}|%\\d*\\$?[sdif%]|[\\x01-\\x1F⟦⟧]");

    interface Provider {
        List<String> translate(List<String> originals, List<String> protectedTexts) throws Exception;
    }

    static Map<String, String> translate(List<String> originals, Provider provider) throws Exception {
        List<String> inputs = new ArrayList<>(originals.size());
        List<List<String>> codes = new ArrayList<>(originals.size());
        for (String original : originals) {
            List<String> tokens = new ArrayList<>();
            inputs.add(shield(original, tokens));
            codes.add(tokens);
        }
        List<String> outputs = provider.translate(originals, inputs);
        if (outputs == null || outputs.size() != originals.size()) {
            throw new IOException("El proveedor devolvió otra cantidad de frases");
        }
        Map<String, String> answers = new LinkedHashMap<>();
        for (int i = 0; i < outputs.size(); i++) {
            String original = originals.get(i);
            String translated = unshield(outputs.get(i), codes.get(i));
            if (isSafe(original, translated)) answers.put(original, translated);
        }
        return answers;
    }

    private static String shield(String original, List<String> codes) {
        Matcher matcher = CODES.matcher(original);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "⟦" + codes.size() + "⟧");
            codes.add(matcher.group());
        }
        return matcher.appendTail(result).toString();
    }

    private static String unshield(String output, List<String> codes) {
        if (output == null) return null;
        StringBuilder result = new StringBuilder();
        int position = 0;
        for (int i = 0; i < codes.size(); i++) {
            int next = output.indexOf('⟦', position);
            String marker = "⟦" + i + "⟧";
            if (next < 0 || !output.startsWith(marker, next)) return null;
            result.append(output, position, next).append(codes.get(i));
            position = next + marker.length();
        }
        if (output.indexOf('⟦', position) >= 0) return null;
        return result.append(output, position, output.length()).toString();
    }

    static boolean needsContext(String original) {
        return CODES.matcher(original).find() || NeuralText.normalizes(original);
    }

    static String visibleText(String text) {
        return CODES.matcher(text).replaceAll(" ");
    }

    /** Check existing rows and restored results for added, dropped, or reordered game tokens. */
    static boolean isSafe(String original, String translated) {
        if (translated == null || translated.trim().isEmpty()) return false;
        Matcher expected = CODES.matcher(original);
        Matcher actual = CODES.matcher(translated);
        while (expected.find()) {
            if (!actual.find() || !expected.group().equals(actual.group())) return false;
        }
        return !actual.find();
    }
}
