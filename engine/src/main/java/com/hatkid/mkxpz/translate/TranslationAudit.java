package com.hatkid.mkxpz.translate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative English-to-Spanish checks shared by preparation and coverage. */
public final class TranslationAudit {
    public enum Issue { MISSING, OUTDATED, FORMAT, UNCHANGED, ENGLISH, TERMINOLOGY, NAMES, REVIEW }

    private static final Pattern ENGLISH = Pattern.compile(
        "\\b(?:jammies|gonna|we['’]ve|you['’]re|they['’]re|we['’]ll)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern RETAINED_POSSESSIVE = Pattern.compile("\\{[0-9]+(?::[^}]+)?\\}[’']s\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern DAMAGED_WORD = Pattern.compile("[A-Za-z]\\.{1,2}[A-Za-z]");
    private static final Pattern ABBREVIATION = Pattern.compile("\\b(?:Sp\\.(?:Atk|Def)|(?:[A-Z]\\.){2,}[A-Z]?)");
    private static final Pattern ORDINAL = Pattern.compile("\\b[0-9]+(?:st|nd|rd|th)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIXED_CODE = Pattern.compile("\\b(?=[A-Za-z0-9]{4,}\\b)(?=[A-Za-z0-9]*[013457])(?=[A-Za-z0-9]*[A-Za-z])[A-Za-z0-9]+\\b");

    private TranslationAudit() {}

    static Map<String, String> translate(List<String> originals, TranslationBatch.Provider provider) throws Exception {
        return translate(originals, provider, TranslationProfile.EMPTY);
    }

    static Map<String, String> translate(List<String> originals, TranslationBatch.Provider provider, TranslationProfile profile) throws Exception {
        Map<String, String> answers = new LinkedHashMap<>();
        Map<String, TranslationChoices.Document> choices = new LinkedHashMap<>();
        java.util.Set<String> fragments = new java.util.LinkedHashSet<>();
        for (String original : originals) {
            TranslationChoices.Document document = TranslationChoices.parse(original);
            if (document != null) {
                choices.put(original, document);
                for (TranslationChoices.Part part : document.parts) {
                    if (part.fixed == null && !part.text.trim().isEmpty()) fragments.add(part.text);
                }
            } else if (!TranslationChoices.contains(original)) fragments.add(original);
        }
        List<String> pending = new ArrayList<>();
        for (String original : fragments) {
            String term = profile.preserve(original) || TranslationProfile.internal(original)
                ? original : TranslationLabels.fixed(original, profile);
            if (term == null) pending.add(original);
            else answers.put(original, term);
        }
        if (!pending.isEmpty()) answers.putAll(TranslationBatch.translate(pending, provider));
        Map<String, String> complete = new LinkedHashMap<>();
        for (String original : originals) {
            TranslationChoices.Document document = choices.get(original);
            String answer = document == null ? answers.get(original) : document.rebuild(answers);
            if (answer != null) complete.put(original, answer);
        }
        return complete;
    }

    static Issue check(String original, String answer) {
        return check(original, answer, TranslationProfile.EMPTY);
    }

    static Issue check(String original, String answer, TranslationProfile profile) {
        if (answer == null || answer.trim().isEmpty()) return Issue.MISSING;
        if (TranslationChoices.contains(original) || TranslationChoices.contains(answer)) {
            TranslationChoices.Document source = TranslationChoices.parse(original);
            TranslationChoices.Document translated = TranslationChoices.parse(answer);
            if (!TranslationChoices.sameStructure(source, translated)) return Issue.FORMAT;
            for (int i = 0; i < source.parts.size(); i++) {
                TranslationChoices.Part part = source.parts.get(i);
                String result = translated.parts.get(i).text;
                if (part.text.trim().isEmpty()) {
                    if (!part.text.equals(result)) return Issue.FORMAT;
                } else if (part.fixed != null) {
                    if (!part.fixed.equals(result)) return Issue.TERMINOLOGY;
                } else {
                    Issue issue = check(part.text, result, profile);
                    if (issue != null) return issue;
                }
            }
            return null;
        }
        if (!TranslationBatch.isSafe(original, answer)) return Issue.FORMAT;
        if (profile.preserve(original) || TranslationProfile.internal(original)) return original.equals(answer) ? null : Issue.FORMAT;
        if (original.equals(answer)) return Issue.UNCHANGED;
        if (TranslationDialogue.possibleCreditList(original)) return Issue.NAMES;
        String term = TranslationLabels.fixed(original, profile);
        if (term != null && !term.equalsIgnoreCase(answer)) return Issue.TERMINOLOGY;
        if (!TranslationLabels.retainsListTerms(original, answer)) return Issue.TERMINOLOGY;
        if (!TranslationDialogue.sameSpeaker(original, answer)) return Issue.NAMES;

        if (RETAINED_POSSESSIVE.matcher(original).find() && RETAINED_POSSESSIVE.matcher(answer).find()) return Issue.ENGLISH;

        String rendered = TranslationBatch.visibleText(answer);
        String source = TranslationBatch.visibleText(original);
        if (WORDS.matcher(source).find() && !WORDS.matcher(rendered).find()) return Issue.REVIEW;
        // A distorted source needs review; punctuation changes are not a translation.
        String body = TranslationBatch.visibleText(TranslationDialogue.body(original));
        Matcher damaged = DAMAGED_WORD.matcher(ABBREVIATION.matcher(body).replaceAll(" "));
        if (damaged.find() && damaged.find()) return Issue.REVIEW;
        String withoutOrdinals = ORDINAL.matcher(body).replaceAll(" ");
        if (MIXED_CODE.matcher(withoutOrdinals).find()
                && MIXED_CODE.matcher(TranslationDialogue.normalize(withoutOrdinals)).find()) return Issue.REVIEW;
        if (ORDINAL.matcher(source).find() && ORDINAL.matcher(rendered).find()) return Issue.ENGLISH;
        // Known invariant labels/names were classified above. Punctuation alone is no progress.
        if (sameWords(source, rendered)) return Issue.UNCHANGED;
        if (!TranslationDialogue.speaker(original).isEmpty() && !profile.preserve(TranslationDialogue.body(original))
                && sameWords(TranslationBatch.visibleText(TranslationDialogue.body(original)),
                    TranslationBatch.visibleText(answer.substring(answer.indexOf(':') + 1)))) return Issue.UNCHANGED;

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
        return count >= 1 && !right.find();
    }
}
