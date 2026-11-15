package com.hatkid.mkxpz.translate;

import java.util.*;
import java.util.regex.Pattern;

/** RGSS choice labels are text; variable/cancel IDs and separators are code. */
final class TranslationChoices {
    private static final Pattern VARIABLE = Pattern.compile("[ \\t]*[0-9]+[ \\t]*");
    private static final Pattern CANCEL = Pattern.compile("[ \\t]*-?[0-9]+[ \\t]*");
    private static final Pattern ATTRIBUTES = Pattern.compile("[\\p{L}\\s'’-]+(?:/[\\p{L}\\s'’-]+)+");

    static final class Part {
        final String before, text;
        String fixed;
        Part(String before, String text) { this.before = before; this.text = text; }
    }
    static final class Document {
        final List<Part> parts = new ArrayList<>();
        String rebuild(Map<String, String> answers) {
            StringBuilder result = new StringBuilder();
            for (Part part : parts) {
                String translated = part.fixed != null ? part.fixed : answers.get(part.text);
                if (part.text.trim().isEmpty()) translated = part.text;
                if (translated == null) return null;
                result.append(part.before).append(translated);
            }
            return result.toString();
        }
    }

    static boolean contains(String text) { return text.contains("\\ch["); }

    // Unsupported/malformed commands remain pending. Never repair their IDs.
    static Document parse(String text) {
        if (!contains(text)) return null;
        Document result = new Document();
        int cursor = 0;
        String before = "";
        while (true) {
            int start = text.indexOf("\\ch[", cursor);
            if (start < 0) break;
            if (start > 0 && text.charAt(start - 1) == '\\') return null;
            int first = text.indexOf(',', start + 4), second = text.indexOf(',', first + 1);
            if (first < 0 || second < 0 || !VARIABLE.matcher(text.substring(start + 4, first)).matches()
                    || !CANCEL.matcher(text.substring(first + 1, second)).matches()) return null;
            result.parts.add(new Part(before, text.substring(cursor, start)));
            int labelStart = second + 1, depth = 0, end = -1;
            before = text.substring(start, labelStart);
            List<Part> labels = new ArrayList<>();
            for (int at = labelStart; at < text.length(); at++) {
                if (text.startsWith("\\ch[", at)) return null;
                char c = text.charAt(at);
                if (c == '<') {
                    int close = text.indexOf('>', at + 1);
                    if (close >= 0 && close - at <= 40 && text.substring(at, close).indexOf('\n') < 0) {
                        at = close;
                        continue;
                    }
                }
                if (c == '[') depth++;
                if (c == ']' && depth > 0) { depth--; continue; }
                if ((c == ',' || c == ']') && depth == 0) {
                    Part label = new Part(before, text.substring(labelStart, at));
                    if (label.text.trim().isEmpty()) return null;
                    labels.add(label);
                    before = ",";
                    labelStart = at + 1;
                    if (c == ']') { end = at; break; }
                }
            }
            if (end < 0) return null;
            applyTypedLabels(labels, NeuralLabels.TYPES);
            applyTypedLabels(labels, NeuralLabels.NATURES);
            for (Part label : labels) {
                if (label.fixed == null && ATTRIBUTES.matcher(label.text).matches()) {
                    String[] attributes = label.text.split("/", -1);
                    for (int i = 0; i < attributes.length; i++) result.parts.add(new Part(i == 0 ? label.before : "/", attributes[i]));
                } else result.parts.add(label);
            }
            cursor = end + 1;
            before = "]";
        }
        result.parts.add(new Part(before, text.substring(cursor)));
        return result;
    }

    private static void applyTypedLabels(List<Part> labels, Map<String, String> glossary) {
        if (labels.size() < 3) return;
        int known = 0;
        for (Part label : labels) {
            String key = label.text.trim();
            if (glossary.containsKey(key)) known++;
            else if (!key.equals("Cancel") && !key.equals("Back")) return;
        }
        if (known < 3) return;
        for (Part label : labels) {
            String key = label.text.trim(), value = glossary.get(key);
            if (value == null) value = key.equals("Cancel") ? "Cancelar" : "Volver";
            int offset = label.text.indexOf(key);
            label.fixed = label.text.substring(0, offset) + value + label.text.substring(offset + key.length());
        }
    }

    static boolean sameStructure(Document left, Document right) {
        if (left == null || right == null || left.parts.size() != right.parts.size()) return false;
        for (int i = 0; i < left.parts.size(); i++) {
            if (!left.parts.get(i).before.equals(right.parts.get(i).before)) return false;
        }
        return true;
    }

    private TranslationChoices() {}
}
