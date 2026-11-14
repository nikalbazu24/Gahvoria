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

    private static final Pattern VALUES = Pattern.compile(
        "\\{[0-9]+(?::[A-Za-z0-9. +#-]+)?\\}|%[0-9]+\\$[sdif]|"
        + "(?i)\\\\(?:PN|PM|PG|V\\[[0-9]+\\]|N\\[[0-9]+\\])");
    private static final Pattern MARKERS = Pattern.compile("⟦([0-9]+)⟧");

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
        Matcher markers = MARKERS.matcher(output);
        StringBuffer result = new StringBuffer();
        boolean[] seen = new boolean[codes.size()];
        int count = 0;
        while (markers.find()) {
            int index;
            try { index = Integer.parseInt(markers.group(1)); }
            catch (NumberFormatException invalid) { return null; }
            if (index >= codes.size() || seen[index]) return null;
            seen[index] = true;
            count++;
            markers.appendReplacement(result, Matcher.quoteReplacement(codes.get(index)));
        }
        markers.appendTail(result);
        if (count != codes.size()) return null;
        return result.toString();
    }

    static List<String> tokens(String original) {
        List<String> result = new ArrayList<>();
        Matcher matcher = CODES.matcher(original);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }

    // Only named/numbered values may change position. Effects, colors, pauses and
    // unnumbered printf arguments must keep their order and value boundaries.
    static boolean movable(String token) {
        return VALUES.matcher(token).matches();
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
        List<String> expected = tokens(original);
        List<String> actual = tokens(translated);
        if (expected.size() != actual.size()) return false;
        Map<String, Integer> values = new LinkedHashMap<>();
        int next = 0;
        for (String token : expected) {
            if (movable(token)) {
                values.put(token, values.containsKey(token) ? values.get(token) + 1 : 1);
                continue;
            }
            while (next < actual.size() && movable(actual.get(next))) {
                String value = actual.get(next++);
                values.put(value, values.containsKey(value) ? values.get(value) - 1 : -1);
            }
            for (int count : values.values()) if (count != 0) return false;
            values.clear();
            if (next >= actual.size() || !token.equals(actual.get(next++))) return false;
        }
        while (next < actual.size()) {
            String value = actual.get(next++);
            if (!movable(value)) return false;
            values.put(value, values.containsKey(value) ? values.get(value) - 1 : -1);
        }
        for (int count : values.values()) if (count != 0) return false;
        return true;
    }
}
