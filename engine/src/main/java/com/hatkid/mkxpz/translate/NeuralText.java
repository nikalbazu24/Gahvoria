package com.hatkid.mkxpz.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Keep RPG Maker markers outside SentencePiece and restore them in their exact order. */
final class NeuralText {
    private static final Pattern MARKER = Pattern.compile("⟦([0-9]+)⟧");

    interface Inference {
        List<String> run(List<String> texts) throws IOException;
    }

    private static final Pattern ALIGNED = Pattern.compile("<img data-gahvoria=\"([0-9]+)\"\\s*/?>");
    private static final Pattern LEADING = Pattern.compile("\\A(?:\\s*⟦[0-9]+⟧)+");
    private static final Pattern ENTITY = Pattern.compile("&(amp|lt|gt);");
    private static final Pattern PAJAMAS = Pattern.compile("\\bjammies\\b", Pattern.CASE_INSENSITIVE);

    static boolean normalizes(String text) {
        return PAJAMAS.matcher(text).find();
    }

    /** Pass one complete utterance through the model even when RGSS inserts controls inside it. */
    static List<String> translate(List<String> texts, Inference plain, Inference aligned) throws IOException {
        List<String> result = new ArrayList<>(texts);
        List<Integer> rows = new ArrayList<>();
        List<String> markup = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (MARKER.matcher(text).find() && hasLetters(MARKER.matcher(text).replaceAll(""))) {
                rows.add(i);
                String escaped = PAJAMAS.matcher(text).replaceAll("pajamas")
                    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                markup.add(MARKER.matcher(escaped).replaceAll("<img data-gahvoria=\"$1\">"));
            }
        }
        boolean[] restored = new boolean[texts.size()];
        for (int start = 0; start < markup.size(); start += 24) {
            int end = Math.min(start + 24, markup.size());
            List<String> outputs;
            try {
                outputs = aligned.run(markup.subList(start, end));
            } catch (IOException | RuntimeException unsupported) {
                continue;
            }
            if (outputs == null || outputs.size() != end - start) continue;
            for (int i = start; i < end; i++) {
                int row = rows.get(i);
                String translation = restore(texts.get(row), outputs.get(i - start));
                if (translation != null) {
                    result.set(row, translation);
                    restored[row] = true;
                }
            }
        }
        List<String> fallback = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            if (!restored[i]) fallback.add(PAJAMAS.matcher(texts.get(i)).replaceAll("pajamas"));
        }
        List<String> translated = translate(fallback, plain);
        int next = 0;
        for (int i = 0; i < texts.size(); i++) {
            if (!restored[i]) result.set(i, translated.get(next++));
        }
        return result;
    }

    private static String restore(String original, String markup) {
        if (markup == null || markup.indexOf('⟦') >= 0 || markup.indexOf('⟧') >= 0) return null;
        String restored = ALIGNED.matcher(markup).replaceAll("⟦$1⟧");
        if (restored.indexOf('<') >= 0 || restored.indexOf('>') >= 0) return null;
        // Leading controls configure the box before any glyph is printed.
        Matcher leading = LEADING.matcher(original);
        if (leading.find() && !restored.startsWith(leading.group())) return null;
        if (restored.length() > original.length() * 5 + 120) return null;
        Matcher expected = MARKER.matcher(original);
        Matcher actual = MARKER.matcher(restored);
        while (expected.find()) {
            if (!actual.find() || !expected.group().equals(actual.group())) return null;
        }
        if (actual.find() || !hasLetters(MARKER.matcher(restored).replaceAll(""))) return null;
        Matcher entities = ENTITY.matcher(restored);
        StringBuffer decoded = new StringBuffer();
        while (entities.find()) {
            String entity = entities.group(1);
            entities.appendReplacement(decoded, entity.equals("amp") ? "&" : entity.equals("lt") ? "<" : ">");
        }
        return entities.appendTail(decoded).toString();
    }

    static List<String> translate(List<String> texts, Inference inference) throws IOException {
        List<String> result = new ArrayList<>(texts.size());
        List<List<String>> rows = new ArrayList<>(texts.size());
        List<String> inputs = new ArrayList<>();
        for (String text : texts) {
            List<String> segments = new ArrayList<>();
            Matcher matcher = MARKER.matcher(text);
            int start = 0;
            while (matcher.find()) {
                segments.add(text.substring(start, matcher.start()));
                start = matcher.end();
            }
            segments.add(text.substring(start));

            rows.add(segments);
            for (String segment : segments) if (hasLetters(segment)) inputs.add(segment.trim());
        }
        List<String> outputs = new ArrayList<>(inputs.size());
        for (int start = 0; start < inputs.size(); start += 32) {
            List<String> batch = inputs.subList(start, Math.min(start + 32, inputs.size()));
            List<String> translated = inference.run(batch);
            if (translated.size() != batch.size()) throw new IOException("Respuesta incompleta del modelo Neural");
            outputs.addAll(translated);
        }

        int index = 0;
        for (int row = 0; row < texts.size(); row++) {
            Matcher matcher = MARKER.matcher(texts.get(row));
            StringBuilder joined = new StringBuilder();
            boolean valid = true;
            for (String segment : rows.get(row)) {
                if (hasLetters(segment)) {
                    String translated = outputs.get(index++);
                    if (translated == null || translated.trim().isEmpty()) {
                        valid = false;
                        continue;
                    }
                    int left = 0;
                    int right = segment.length();
                    while (left < right && segment.charAt(left) <= ' ') left++;
                    while (right > left && segment.charAt(right - 1) <= ' ') right--;
                    joined.append(segment, 0, left).append(translated.trim()).append(segment, right, segment.length());
                } else {
                    joined.append(segment);
                }
                if (matcher.find()) joined.append(matcher.group());
            }
            result.add(valid ? joined.toString() : null);
        }
        return result;
    }

    private static boolean hasLetters(String text) {
        for (int i = 0; i < text.length(); i++) if (Character.isLetter(text.charAt(i))) return true;
        return false;
    }
}
