package com.hatkid.mkxpz.translate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative English-to-Spanish checks shared by preparation and coverage. */
public final class TranslationAudit {
    public enum Issue { MISSING, OUTDATED, FORMAT, UNCHANGED, ENGLISH, TERMINOLOGY }

    private static final Pattern ENGLISH = Pattern.compile(
        "\\b(?:jammies|gonna|we['’]ve|you['’]re|they['’]re|we['’]ll)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]+");

    private TranslationAudit() {}

    private static String glossary(String original) {
        if ("Pokémon Center".equalsIgnoreCase(original)) return "Centro Pokémon";
        if ("Pokémon League".equalsIgnoreCase(original)) return "Liga Pokémon";
        return null;
    }

    static Map<String, String> translate(List<String> originals, TranslationBatch.Provider provider) throws Exception {
        Map<String, String> answers = new LinkedHashMap<>();
        List<String> pending = new ArrayList<>();
        for (String original : originals) {
            String term = glossary(original);
            if (term == null) pending.add(original);
            else answers.put(original, term);
        }
        if (!pending.isEmpty()) answers.putAll(TranslationBatch.translate(pending, provider));
        return answers;
    }

    static Issue check(String original, String answer) {
        if (answer == null || answer.trim().isEmpty()) return Issue.MISSING;
        if (!TranslationBatch.isSafe(original, answer)) return Issue.FORMAT;
        if (original.equals(answer)) return Issue.UNCHANGED;
        String term = glossary(original);
        if (term != null && !term.equalsIgnoreCase(answer)) return Issue.TERMINOLOGY;

        String rendered = TranslationBatch.visibleText(answer);
        String source = TranslationBatch.visibleText(original);
        // Keep short names and labels out of this case- and punctuation-insensitive check.
        if (sameWords(source, rendered)) return Issue.UNCHANGED;

        Matcher english = ENGLISH.matcher(source);
        while (english.find()) {
            Matcher retained = ENGLISH.matcher(rendered);
            while (retained.find()) {
                if (english.group().replace('’', '\'').equalsIgnoreCase(retained.group().replace('’', '\''))) {
                    return Issue.ENGLISH;
                }
            }
        }
        if (NeuralText.hasColloquialConnector(source) && NeuralText.hasColloquialConnector(rendered)) {
            return Issue.ENGLISH;
        }
        return null;
    }

    private static boolean sameWords(String source, String rendered) {
        Matcher left = WORDS.matcher(source);
        Matcher right = WORDS.matcher(rendered);
        int count = 0;
        while (left.find()) {
            if (!right.find() || !left.group().equalsIgnoreCase(right.group())) return false;
            count++;
        }
        return count >= 3 && !right.find();
    }
}
