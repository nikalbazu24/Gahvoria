package com.hatkid.mkxpz.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
    private static final Pattern CAUSAL_CONNECTOR = Pattern.compile(
        "(^|[.!?]\\s+)(?:cuz|'cause|’cause)(?=[,\\s])", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTRACTION = Pattern.compile(
        "\\b(?:we['’](?:ve|ll)|(?:you|they)['’]re|gonna)\\b", Pattern.CASE_INSENSITIVE);

    static boolean hasColloquialConnector(String text) {
        return CAUSAL_CONNECTOR.matcher(text).find();
    }

    static boolean normalizes(String text) {
        return PAJAMAS.matcher(text).find() || hasColloquialConnector(text);
    }

    private static String normalize(String text) {
        String normalized = PAJAMAS.matcher(text).replaceAll("pajamas");
        normalized = CAUSAL_CONNECTOR.matcher(normalized).replaceAll("$1Because");
        Matcher contraction = CONTRACTION.matcher(normalized);
        StringBuffer expanded = new StringBuffer();
        while (contraction.find()) {
            String word = contraction.group().replace('’', '\'');
            int apostrophe = word.indexOf('\'');
            String replacement = Character.isUpperCase(word.charAt(0)) ? "Going to" : "going to";
            if (apostrophe >= 0) {
                String ending = word.substring(apostrophe + 1);
                String verb = ending.equalsIgnoreCase("ve") ? " have" : ending.equalsIgnoreCase("ll") ? " will" : " are";
                replacement = word.substring(0, apostrophe) + verb;
            }
            contraction.appendReplacement(expanded, replacement);
        }
        return contraction.appendTail(expanded).toString();
    }

    static boolean usesValueSlots(String text) {
        for (String token : TranslationBatch.tokens(text)) if (TranslationBatch.movable(token)) return true;
        return false;
    }

    private static final Pattern NUMBER_FORMAT = Pattern.compile(".*:[ 0-9.+#-]*[dif]\\}");
    private static final Pattern QUANTITY_NOUN = Pattern.compile(
        "(?i)^\\s+(?:coins|points|badges|minutes|seconds|hours|days|rooms|steps|levels|items|times|HP|PP)\\b.*", Pattern.DOTALL);

    private static boolean numericSlot(String original, String token) {
        if (NUMBER_FORMAT.matcher(token).matches()) return true;
        int at = original.indexOf(token);
        return at >= 0 && QUANTITY_NOUN.matcher(original.substring(at + token.length())).matches();
    }

    private static String slot(int index) {
        String suffix = "";
        do { suffix = (char) ('A' + index % 26) + suffix; index = index / 26 - 1; } while (index >= 0);
        return "ZXQV" + suffix;
    }

    private static final Pattern POSSESSIVE = Pattern.compile("['’]s\\s+", Pattern.CASE_INSENSITIVE);
    // These ordinary nouns remain visible to the model. Rephrase the English
    // possessive as "the dreams of VALUE", avoiding the ambiguous VALUE's token.
    private static final Pattern ORDINARY_NOUN = Pattern.compile(
        "(?i)(?:special attack and special defense|special attack|special defense|"
        + "health|dreams|friends|family|parents|mother|father|brother|sister|"
        + "bag|book|house|team|score|name|attack|defense|speed|accuracy|evasion)"
        + "(?![\\p{L}\\p{N}_'’-])");

    private static boolean nounBoundary(String text, int end) {
        return end == text.length() || (!Character.isLetterOrDigit(text.charAt(end))
            && "_'-’".indexOf(text.charAt(end)) < 0);
    }

    private static String[] abilityAt(String text, int start) {
        for (String[] term : NeuralTerms.ABILITIES) {
            if (text.startsWith(term[0], start) && nounBoundary(text, start + term[0].length())) return term;
        }
        for (String[] term : new String[][]{{"Z-Power", "poder Z"}, {"Crest", "emblema"}}) {
            if (text.startsWith(term[0], start) && nounBoundary(text, start + term[0].length())) return term;
        }
        return null;
    }

    private static final class ValueGroup {
        final int index, start;
        int end;
        String expansion;
        String nounPrefix = "";
        boolean combined, literalNoun;
        ValueGroup(int index, int start, int end, String expansion) {
            this.index = index; this.start = start; this.end = end; this.expansion = expansion;
        }
    }

    // A dynamic possessive is one noun phrase. Keep its values together during
    // inference, then expand "{1}'s {2}" as "{2} de {1}". Adjacent values such as
    // passwords also form one unit, retaining their original concatenation.
    // Never group across an RGSS effect, color, pause or unnumbered printf value.
    private static List<ValueGroup> groups(String original, String text) {
        List<String> tokens = TranslationBatch.tokens(original);
        List<ValueGroup> groups = new ArrayList<>();
        Matcher markers = MARKER.matcher(text);
        ValueGroup previous = null;
        while (markers.find()) {
            int index = Integer.parseInt(markers.group(1));
            if (index >= tokens.size() || !TranslationBatch.movable(tokens.get(index))) {
                previous = null;
                continue;
            }
            String gap = previous == null ? null : text.substring(previous.end, markers.start());
            boolean possessive = gap != null && POSSESSIVE.matcher(gap).matches()
                && !numericSlot(original, tokens.get(index));
            if (previous != null && (gap.isEmpty() || possessive)) {
                previous.expansion = possessive ? markers.group() + " de " + previous.expansion
                                               : previous.expansion + markers.group();
                previous.end = markers.end();
                previous.combined = true;
            } else {
                previous = new ValueGroup(index, markers.start(), markers.end(), markers.group());
                groups.add(previous);
            }
        }
        for (ValueGroup group : groups) {
            Matcher possessive = POSSESSIVE.matcher(text).region(group.end, text.length());
            if (!possessive.lookingAt()) continue;
            int start = possessive.end();
            String[] term = abilityAt(text, start);
            if (term != null) {
                group.expansion = term[1] + " de " + group.expansion;
                group.end = start + term[0].length();
                group.combined = true;
                group.literalNoun = true;
            } else {
                Matcher noun = ORDINARY_NOUN.matcher(text).region(start, text.length());
                if (!noun.lookingAt()) continue;
                group.nounPrefix = "the " + noun.group() + " of ";
                group.end = noun.end();
                group.literalNoun = true;
            }
        }
        return groups;
    }

    private static boolean letterAt(String text, int index) {
        return index >= 0 && index < text.length() && Character.isLetter(text.charAt(index));
    }

    static boolean needsSlotRepair(String original) {
        String protectedText = TranslationBatch.shield(original, new ArrayList<>());
        for (ValueGroup group : groups(original, protectedText)) {
            if (group.combined || letterAt(protectedText, group.start - 1) || letterAt(protectedText, group.end)) return true;
        }
        return false;
    }

    static boolean needsNounRepair(String original) {
        if (original.indexOf('\'') < 0 && original.indexOf('’') < 0) return false;
        String protectedText = TranslationBatch.shield(original, new ArrayList<>());
        for (ValueGroup group : groups(original, protectedText)) if (group.literalNoun) return true;
        return false;
    }

    /** Keep grammatical values visible to the model, without sending real names.
     * Restore each unit exactly once; incomplete or duplicated units are rejected. */
    static List<String> translate(List<String> originals, List<String> texts,
                                  Inference plain, Inference aligned) throws IOException {
        List<String> prepared = new ArrayList<>(texts);
        List<Map<String, String>> slots = new ArrayList<>();
        for (int row = 0; row < texts.size(); row++) {
            Map<String, String> values = new LinkedHashMap<>();
            String text = texts.get(row);
            if (!text.toUpperCase(java.util.Locale.ROOT).contains("ZXQV")) {
                String original = originals.get(row);
                List<String> tokens = TranslationBatch.tokens(original);
                StringBuilder input = new StringBuilder();
                int cursor = 0;
                for (ValueGroup group : groups(original, text)) {
                    String value = slot(group.index);
                    if (!group.combined && numericSlot(original, tokens.get(group.index))) {
                        int number = 73189 + group.index;
                        while (original.contains(String.valueOf(number)) || values.containsKey(String.valueOf(number))) number++;
                        value = String.valueOf(number);
                    }
                    values.put(value, group.expansion);
                    input.append(text, cursor, group.start);
                    // A variable next to a verb is not part of the verb's spelling.
                    if (letterAt(text, group.start - 1)) input.append(' ');
                    input.append(group.nounPrefix).append(value);
                    if (letterAt(text, group.end)) input.append(' ');
                    cursor = group.end;
                }
                text = input.append(text, cursor, text.length()).toString();
            }
            prepared.set(row, text);
            slots.add(values);
        }
        List<String> result = translate(prepared, plain, aligned);
        for (int row = 0; row < result.size(); row++) {
            if (slots.get(row).isEmpty() || result.get(row) == null) continue;
            String output = result.get(row);
            boolean valid = true;
            for (Map.Entry<String, String> entry : slots.get(row).entrySet()) {
                Matcher token = Pattern.compile("(?i)(?<![\\p{L}\\p{N}_])" + entry.getKey() + "(?![\\p{L}\\p{N}_])").matcher(output);
                if (!token.find()) { valid = false; break; }
                int start = token.start(), end = token.end();
                if (token.find()) { valid = false; break; }
                output = output.substring(0, start) + entry.getValue() + output.substring(end);
            }
            if (output.toUpperCase(java.util.Locale.ROOT).contains("ZXQV")) valid = false;
            result.set(row, valid ? output : null);
        }
        return result;
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
                String escaped = normalize(text)
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
            if (!restored[i]) fallback.add(normalize(texts.get(i)));
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
