package com.hatkid.mkxpz.translate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Per-catalog names. A single label is insufficient evidence to invent a name. */
final class TranslationProfile {
    static final TranslationProfile EMPTY = new TranslationProfile(Collections.emptySet());
    private final Set<String> names;
    private TranslationProfile(Set<String> names) { this.names = names; }
    static TranslationProfile read(File catalog) throws IOException {
        return read(catalog, new TranslationWork());
    }
    static TranslationProfile read(File catalog, TranslationWork work) throws IOException {
        work.checkpoint(TranslationWork.Phase.CATALOG, 0);
        if (!catalog.isFile()) return EMPTY;
        Map<String, Integer> counts = new HashMap<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(catalog), StandardCharsets.UTF_8))) {
            String row;
            int reviewed = 0;
            while ((row = input.readLine()) != null) {
                if ((reviewed++ & 255) == 0) work.checkpoint(TranslationWork.Phase.CATALOG, reviewed);
                String text = TranslationMemory.unescape(row);
                if (text == null) continue;
                String prefix = TranslationDialogue.speaker(text).trim();
                if (prefix.isEmpty() || !prefix.equals(TranslationDialogue.localizedSpeaker(text).trim())) continue;
                String name = prefix.substring(0, prefix.length() - 1).toLowerCase(Locale.ROOT);
                if (name.matches("[\\p{L}]+")) counts.put(name, counts.getOrDefault(name, 0) + 1);
            }
        }
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) if (entry.getValue() >= 2) names.add(entry.getKey());
        return new TranslationProfile(names);
    }
    private static final Pattern SOUNDS = Pattern.compile("(?i)(?:a+h+|o+h+|h+m+|m+h*m+|n{2,}|g+r+|r{2,}|z{2,}|sh+h*|ps+s*t|no)");
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]+(?:['’][\\p{L}]+)?");
    private static final Pattern NUMBER_OR_KEY = Pattern.compile("[0-9]+|f(?:[1-9]|1[0-2])");
    private static final Pattern INTERNAL = Pattern.compile("\\[(?:GLOBAL|MAP[0-9]+)\\]|(?i:(?:Graphics|Audio|Data|Pokedex|Pictures|Characters|Battlers|Icons|Fonts)/[A-Za-z0-9_./-]+)");
    private static final Pattern UNKNOWN_MARKUP = Pattern.compile("<(?!/?(?:ac|al|ar|br|r|b|i|u|s|c|c2|c3|fn|fs)(?:>|=|\\s))|(?<!\\\\)\\[[A-Za-z_]+\\]");
    private static final Set<String> INVARIANT = new HashSet<>(Arrays.asList(
        "pokémon", "pokédex", "pp", "ivs", "evs", "id", "pc", "gts", "m", "kg", "cm", "mm", "km",
        "normal", "simple", "retro", "general", "total", "color", "tempo", "chocolate", "doctor"));
    boolean preserve(String original) {
        String prefix = TranslationDialogue.speaker(original);
        if (!prefix.equals(TranslationDialogue.localizePrefix(prefix))) return false;
        if (original.contains("\\ch[") || original.contains("\\choice[")) return false;
        if (UNKNOWN_MARKUP.matcher(original).find()) return false;
        if (TranslationLabels.invariant(original)) return true;
        String text = TranslationBatch.visibleText(original.substring(prefix.length())).trim();
        if (text.isEmpty()) return true;
        Matcher word = WORDS.matcher(text);
        while (word.find()) {
            if (preserveWord(word.group())) continue;
            return false;
        }
        return true;
    }
    boolean preserveWord(String word) {
        String value = word.toLowerCase(Locale.ROOT);
        return NUMBER_OR_KEY.matcher(value).matches() || INVARIANT.contains(value)
            || names.contains(value) || SOUNDS.matcher(value).matches();
    }
    static boolean internal(String original) {
        return INTERNAL.matcher(original).matches();
    }
}
