package com.hatkid.mkxpz.translate;

import java.util.*;
import java.util.regex.*;

/** Structural dialogue handling. Names and format codes are never slang input. */
final class TranslationDialogue {
    private static final Pattern SPEAKER = Pattern.compile("^([\\p{L}0-9?][\\p{L}0-9? .'-]{0,35}):\\s*");
    private static final Set<String> LABELS = new HashSet<>(Arrays.asList(
        "HP", "PP", "AP", "ID", "IDNO", "LV", "LEVEL", "TYPE", "TOTAL", "COLOR", "TEMPO", "SPEED",
        "ATTACK", "DEFENSE", "EXP", "ERROR", "WARNING", "NOTICE", "NOTE", "HINT", "NAME", "TIME", "FIGHT", "BAG", "RUN", "SAVE", "OPTIONS", "CONTROLS"));
    static String speaker(String text) {
        Matcher m = SPEAKER.matcher(text);
        if (!m.find() || LABELS.contains(m.group(1).toUpperCase(Locale.ROOT))) return "";
        String name = m.group(1);
        if (!name.equals(name.toUpperCase(Locale.ROOT))) return "";
        return m.group();
    }
    static String body(String text) { return text.substring(speaker(text).length()); }
    static String localizedSpeaker(String text) {
        return localizePrefix(speaker(text));
    }
    static String localizePrefix(String prefix) {
        if (prefix.isEmpty()) return prefix;
        int colon = prefix.indexOf(':');
        String role = prefix.substring(0, colon);
        String translated = role.equals("GRUNT") ? "RECLUTA" : role.equals("TRAINER") ? "ENTRENADOR"
            : role.equals("BLACKBELT") ? "KARATEKA" : role.equals("SHOPKEEPER") ? "VENDEDOR" : role;
        return translated + prefix.substring(colon);
    }
    static boolean possibleCreditList(String text) {
        if (text.indexOf(',') < 0) return false;
        String[] entries = text.split(",");
        if (entries.length < 6) return false;
        int names = 0;
        for (String entry : entries) {
            String value = entry.trim();
            if (!value.isEmpty() && Character.isUpperCase(value.charAt(0)) && value.split("\\s+").length <= 4) names++;
        }
        return names * 4 >= entries.length * 3;
    }

    static final Map<String, String> SHORT = new LinkedHashMap<>();
    private static final Map<String, String> NORMALIZE = new LinkedHashMap<>();
    private static final Set<String> LEET_WORDS = new HashSet<>(Arrays.asList(
        ("a an and are as at back be but by can cannot come death do does don't error even everyone for friend "
        + "from get give go goodbye got greetings have he hello help here hey hi how i if in is it it's just know "
        + "leave let's life like look love make me motherfucker my never no not nothing now of oh on one only or our "
        + "out over please really recovery right rules said say see she should so some stay stop super system take "
        + "that the their them there these they thing this those time to too unit up us very wait want was way we "
        + "welcome were what when where which who why will with would you your zero initiated malfunction "
        + "anarchy riot raise false prophet curse overload domain forfeits plastic fantastic down again all about care "
        + "level bullshit fucking sense doesn't affect affection blackened essence damned adventure lest awaits").split(" ")));
    static {
        String[][] words = {
            {"um", "eh"}, {"uh", "eh"}, {"uhm", "eh"}, {"erm", "eh"}, {"er", "eh"},
            {"uh huh", "ajá"}, {"uh-huh", "ajá"}, {"nu-uh", "qué va"}, {"huh", "eh"},
            {"hmph", "jum"}, {"tch", "tsk"}, {"tsk", "tsk"}, {"ugh", "uf"}, {"urgh", "uf"},
            {"oof", "uf"}, {"ow", "ay"}, {"ouch", "ay"}, {"ew", "puaj"}, {"yuck", "puaj"},
            {"oops", "ups"}, {"whoops", "ups"}, {"whoa", "vaya"}, {"wow", "vaya"},
            {"phew", "uf"}, {"duh", "obvio"}, {"hey", "oye"}, {"yo", "hola"}, {"hi", "hola"},
            {"sup", "qué tal"}, {"howdy", "hola"}, {"bye", "adiós"}, {"cya", "nos vemos"},
            {"gotcha", "entendido"}, {"gottem", "te pillé"}, {"dunno", "no sé"},
            {"checkmate", "jaque mate"}, {"c'mon", "vamos"}, {"nope", "no"}, {"yep", "sí"},
            {"yeah", "sí"}, {"yeh", "sí"}, {"yes", "sí"}, {"okay", "vale"}, {"ok", "vale"},
            {"kay", "vale"}, {"'kay", "vale"}, {"noob", "novato"}, {"noobs", "novatos"},
            {"shush", "silencio"}, {"boo", "bu"}, {"achoo", "achís"}, {"peekaboo", "cucú"},
            {"ta-da", "tachán"}, {"ta-daa", "tachán"}, {"bruh", "hermano"}, {"sheesh", "caramba"},
            {"yeesh", "caramba"}, {"geez", "caramba"}, {"adieu", "adiós"}, {"ciao", "chao"}
        };
        for (String[] pair : words) SHORT.put(pair[0], pair[1]);
        String[][] slang = {
            {"dunno", "do not know"}, {"c'mon", "come on"}, {"cya", "see you"},
            {"whaddup", "what is up"}, {"whazzup", "what is up"}, {"wot", "what"}, {"wat", "what"},
            {"thx", "thanks"}, {"plz", "please"}, {"pls", "please"}, {"brb", "be right back"},
            {"tbh", "to be honest"}, {"doin'", "doing"}, {"goin'", "going"}, {"nothin'", "nothing"},
            {"nuthin'", "nothing"}, {"gonna", "going to"}, {"wanna", "want to"}
        };
        for (String[] pair : slang) NORMALIZE.put(pair[0], pair[1]);
    }
    private static final Pattern WORD = Pattern.compile("[\\p{L}0-9]+(?:['’][\\p{L}]+|['’])?");
    private static final Pattern EMPHASIS = Pattern.compile("([a-z])\\1{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern LEET_DIGIT = Pattern.compile("[013457]");
    private static final Pattern MIXED_CASE = Pattern.compile("[a-z][A-Z]");
    private static final Pattern MIXED_DIGIT = Pattern.compile("[A-Za-z][0-9]|[0-9][A-Za-z]");
    private static final Pattern SLANG;
    static {
        StringBuilder choices = new StringBuilder();
        for (String word : NORMALIZE.keySet()) {
            if (choices.length() > 0) choices.append('|');
            choices.append(Pattern.quote(word));
        }
        SLANG = Pattern.compile("(?<![\\p{L}])(?:" + choices + ")(?![\\p{L}])", Pattern.CASE_INSENSITIVE);
    }
    static String baseWord(String text) {
        String lower = text.toLowerCase(Locale.ROOT).replace('’', '\'');
        String shorter = EMPHASIS.matcher(lower).replaceAll("$1");
        // Only collapse expressive spelling when the resulting word is recognized.
        return SHORT.containsKey(shorter) || NORMALIZE.containsKey(shorter) || LEET_WORDS.contains(shorter) ? shorter : lower;
    }
    static String normalize(String text) {
        String prefix = speaker(text);
        String body = text.substring(prefix.length());
        Matcher words = WORD.matcher(body);
        StringBuffer result = new StringBuffer();
        while (words.find()) {
            String raw = words.group(), word = baseWord(raw), replacement = raw;
            if (LETTER.matcher(raw).find() && LEET_DIGIT.matcher(raw).find()) {
                String decoded = word.replace('0', 'o').replace('1', 'i').replace('3', 'e')
                    .replace('4', 'a').replace('5', 's').replace('7', 't');
                if (LEET_WORDS.contains(decoded)) replacement = word = decoded;
            }
            if (NORMALIZE.containsKey(word)) replacement = NORMALIZE.get(word);
            else if (LEET_WORDS.contains(word) && (!raw.equals(word) && !raw.equalsIgnoreCase(word)
                    || MIXED_CASE.matcher(raw).find())) replacement = word;
            words.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        return prefix + words.appendTail(result).toString();
    }
    static boolean normalizes(String original) {
        if (!SLANG.matcher(original.replace('’', '\'')).find() && !EMPHASIS.matcher(original).find()
                && !MIXED_DIGIT.matcher(original).find() && !MIXED_CASE.matcher(original).find()) return false;
        String shielded = TranslationBatch.shield(original, new ArrayList<>());
        return !normalize(shielded).equals(shielded);
    }
    private TranslationDialogue() {}
}
