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
        for (String text : texts) {
            List<String> segments = new ArrayList<>();
            Matcher matcher = MARKER.matcher(text);
            int start = 0;
            while (matcher.find()) {
                segments.add(text.substring(start, matcher.start()));
                start = matcher.end();
            }
            segments.add(text.substring(start));

            List<String> inputs = new ArrayList<>();
            for (String segment : segments) if (hasLetters(segment)) inputs.add(segment.trim());
            if (inputs.isEmpty()) {
                result.add(text);
                continue;
            }
            List<String> outputs = inference.run(inputs);
            if (outputs.size() != inputs.size()) throw new IOException("Respuesta incompleta del modelo Neural");

            StringBuilder joined = new StringBuilder();
            int index = 0;
            boolean valid = true;
            matcher.reset();
            for (String segment : segments) {
                if (hasLetters(segment)) {
                    String translated = outputs.get(index++);
                    if (translated == null || translated.trim().isEmpty()) {
                        valid = false;
                        break;
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
