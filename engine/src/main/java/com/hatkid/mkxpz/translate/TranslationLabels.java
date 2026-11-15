package com.hatkid.mkxpz.translate;

import java.util.*;
import java.util.regex.*;

/** Exact labels and complete short replies, not replacements inside prose. */
final class TranslationLabels {
    private static final Map<String, String> LABELS = new LinkedHashMap<>();
    private static final Map<String, String> FIELDS = new LinkedHashMap<>();
    private static final Set<String> FIELD_HEADS = new HashSet<>();
    private static final Set<String> SHORT_HEADS = new HashSet<>();
    // These move names also occur in ordinary menus. The Ruby bridge has typed
    // move context; a bare catalog string does not. Let the sentence model decide.
    private static final Set<String> CONTEXT_REQUIRED = new HashSet<>(Arrays.asList(
        "Return", "Round", "Pound", "Present", "Charge", "Rest", "Counter", "Conversion",
        "Substitute", "Recycle", "Protect", "Follow Me", "After You", "Helping Hand", "Order Up",
        "Stockpile", "Transform", "Refresh"));
    private static final Pattern WORDS = Pattern.compile("[\\p{L}]+(?:['’][\\p{L}]+)?");
    private static final Pattern EDGE = Pattern.compile("^([^\\p{L}]*)(.*?)([^\\p{L}]*$)", Pattern.DOTALL);
    private static final Pattern FIELD;
    private static final Pattern MARKER = Pattern.compile("⟦[0-9]+⟧");
    static {
        String[][] labels = {
            {"Pokémon Center", "Centro Pokémon"}, {"PokéCenter", "Centro Pokémon"}, {"Pokémon League", "Liga Pokémon"},
            {"Sprite", "Gráfico"}, {"HeavyRain", "Lluvia intensa"}, {"ABLE", "APTO"}, {"Jukebox", "Reproductor"},
            {"Autorunning", "Correr automáticamente"}, {"Set", "Fijo"}, {"Backup", "Respaldo"},
            {"Dex No.", "N.º de Pokédex"}, {"Reborn Pokédex", "Pokédex de Reborn"}, {"Total IV", "Total de IV"},
            {"Jan.", "Ene."}, {"Apr.", "Abr."}, {"Aug.", "Ago."}, {"Dec.", "Dic."},
            {"Feb.", "Feb."}, {"Mar.", "Mar."}, {"Jun.", "Jun."}, {"Jul.", "Jul."}, {"Sep.", "Sep."}, {"Oct.", "Oct."}, {"Nov.", "Nov."},
            {"Fight", "Luchar"}, {"Bag", "Bolsa"}, {"Run", "Huir"}, {"Badges", "Medallas"}, {"Time", "Tiempo"}
        };
        for (String[] pair : labels) LABELS.put(pair[0], pair[1]);
        String[][] fields = {{"IDNo.", "N.º ID"}, {"IDNo", "N.º ID"}, {"Sp. Atk", "At. Esp."}, {"Sp.Atk", "At. Esp."},
            {"Sp. Def", "Def. Esp."}, {"Sp.Def", "Def. Esp."}, {"HP", "PS"}, {"Lv.", "Nv."}, {"Lv", "Nv."},
            {"OT", "EO"}, {"WT", "Peso"}, {"Atk", "At."}, {"Def", "Def."}, {"PP", "PP"}, {"AP", "AP"},
            {"ID", "ID"}, {"Pokédex", "Pokédex"}, {"Pokémon", "Pokémon"}, {"Color", "Color"}};
        for (String[] pair : fields) FIELDS.put(pair[0], pair[1]);
        StringBuilder choices = new StringBuilder();
        for (String field : FIELDS.keySet()) {
            Matcher word = WORDS.matcher(field);
            if (word.find()) FIELD_HEADS.add(word.group());
            if (choices.length() > 0) choices.append('|');
            choices.append(Pattern.quote(field));
        }
        FIELD = Pattern.compile("(?<![\\p{L}])(?:" + choices + ")(?![\\p{L}])");
        for (String reply : TranslationDialogue.SHORT.keySet()) {
            Matcher first = WORDS.matcher(reply);
            if (first.find()) SHORT_HEADS.add(first.group());
        }
    }
    private static String matchCase(String original, String replacement) {
        if (original.length() > 1 && original.equals(original.toUpperCase(Locale.ROOT))) return replacement.toUpperCase(Locale.ROOT);
        if (!original.isEmpty() && Character.isUpperCase(original.charAt(0))) {
            return Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
        }
        return replacement;
    }
    static boolean invariant(String original) {
        String text = original.trim();
        return text.equals(LABELS.get(text)) || text.equals(NeuralLabels.LABELS.get(text));
    }
    static String fixed(String original, TranslationProfile profile) {
        if (original.contains("\\ch[") || original.contains("\\choice[")) return null;
        String trimmed = original.trim();
        String label = LABELS.get(trimmed);
        if (label == null && !CONTEXT_REQUIRED.contains(trimmed)) label = NeuralLabels.LABELS.get(trimmed);
        if (label != null) return original.substring(0, original.indexOf(trimmed)) + label
            + original.substring(original.indexOf(trimmed) + trimmed.length());

        // Most catalog entries are prose. Reject non-label candidates before
        // trying every field pattern at every character of the sentence.
        String firstBody = TranslationDialogue.body(original);
        if (firstBody.indexOf('\\') < 0 && firstBody.indexOf('<') < 0 && firstBody.indexOf('{') < 0
                && firstBody.indexOf('[') < 0 && firstBody.indexOf('%') < 0) {
            Matcher first = WORDS.matcher(firstBody);
            if (first.find()) {
                String word = first.group();
                if (!FIELD_HEADS.contains(word) && !SHORT_HEADS.contains(TranslationDialogue.baseWord(word))
                        && !profile.preserveWord(word) && !invariant(word)) return null;
            }
        }

        List<String> codes = new ArrayList<>();
        String shielded = TranslationBatch.shield(original, codes);
        String visible = MARKER.matcher(shielded).replaceAll("");
        String residue = FIELD.matcher(visible).replaceAll("");
        if (!WORDS.matcher(residue).find()) {
            Matcher m = FIELD.matcher(shielded);
            StringBuffer next = new StringBuffer();
            while (m.find()) m.appendReplacement(next, Matcher.quoteReplacement(FIELDS.get(m.group())));
            String result = m.appendTail(next).toString();
            for (int i = 0; i < codes.size(); i++) result = result.replace("⟦" + i + "⟧", codes.get(i));
            return result;
        }
        String prefix = TranslationDialogue.speaker(original);
        String body = original.substring(prefix.length());
        String localized = TranslationDialogue.localizedSpeaker(original);
        if (!prefix.equals(localized) && profile.preserve(body)) return localized + body;
        prefix = localized;
        // Complete short replies only: prose with an unknown word goes to the model.
        if (body.length() > 160 || !codes.isEmpty()) return null;
        Matcher edges = EDGE.matcher(body);
        if (edges.matches()) {
            String core = edges.group(2);
            String answer = TranslationDialogue.SHORT.get(TranslationDialogue.baseWord(core));
            if (answer != null) return prefix + edges.group(1) + matchCase(core, answer) + edges.group(3);
        }
        Matcher words = WORDS.matcher(body);
        StringBuffer result = new StringBuffer();
        boolean changed = false;
        while (words.find()) {
            String word = words.group();
            String replacement = TranslationDialogue.SHORT.get(TranslationDialogue.baseWord(word));
            if (replacement == null) {
                if (!profile.preserve(word)) return null;
                replacement = word;
            } else { replacement = matchCase(word, replacement); changed |= !word.equals(replacement); }
            words.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        return changed ? prefix + words.appendTail(result).toString() : null;
    }
    private TranslationLabels() {}
}
