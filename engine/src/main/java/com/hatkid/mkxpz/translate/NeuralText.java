package com.hatkid.mkxpz.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Keep RPG Maker markers outside SentencePiece and restore them in their exact order. */
final class NeuralText {
    private static final Pattern MARKER = Pattern.compile("⟦[0-9]+⟧");

    interface Inference {
        List<String> run(List<String> texts) throws IOException;
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
