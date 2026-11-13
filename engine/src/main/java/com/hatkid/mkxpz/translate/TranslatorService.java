package com.hatkid.mkxpz.translate;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates a game's text while it runs. The game's Ruby side (essentia_translate.rb) drops the texts it does not
 * know in "queue/NNN.req" files (one escaped text per line) and reads answers appended to "<lang>.tsv"
 * ("original TAB translation", escaped). This thread answers the requests, so every translation is kept in that
 * file and found instantly the next time, even after restarting the game.
 *
 * Two providers: "mlkit" (Google's on-device translator, free, needs a one-time language download) and "deepl"
 * (the player's own key). Nothing here has a key or an account of its own.
 */
public final class TranslatorService
{
    private static final String TAG = "GahvoriaTranslate";
    public static final String PROVIDER_MLKIT = "mlkit";
    public static final String PROVIDER_DEEPL = "deepl";

    private final Context mContext;
    private final File mReqDir;
    private final File mFile;
    private final File mStatus;
    private final String mProvider;
    private final String mTarget;
    private final String mKey;
    private final Set<String> mKnown = new HashSet<>();
    private volatile boolean mRunning;
    private int mDone;
    private String mMessage = "";

    // ML Kit state (only used by the thread)
    private Translator mTranslator;
    private String mTranslatorSource = "";
    private LanguageIdentifier mIdentifier;
    private String mLockedSource = "";
    private String mLastVote = "";
    private int mVotes;

    public TranslatorService(Context context, File dir, String provider, String target, String key)
    {
        mContext = context.getApplicationContext();
        mTarget = target == null || target.isEmpty() ? "es" : target.toLowerCase(Locale.ROOT);
        mFile = new File(dir, mTarget + ".tsv");
        mReqDir = new File(dir, "queue");
        mStatus = new File(context.getFilesDir(), "translate_status.txt");
        mProvider = provider == null ? PROVIDER_MLKIT : provider;
        mKey = key == null ? "" : key.trim();
    }

    /** ML Kit runs through Google Play services: without them (many handhelds) it can never translate. */
    public static boolean mlkitAvailable(Context c)
    {
        try {
            c.getPackageManager().getPackageInfo("com.google.android.gms", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public File translationsFile() { return mFile; }
    public File requestDir() { return mReqDir; }

    public void start()
    {
        //noinspection ResultOfMethodCallIgnored
        mReqDir.mkdirs();
        File[] old = mReqDir.listFiles();
        if (old != null) for (File f : old) //noinspection ResultOfMethodCallIgnored
            f.delete();
        loadKnown();
        mRunning = true;
        if (PROVIDER_MLKIT.equals(mProvider) && !mlkitAvailable(mContext)) {
            // Say it plainly instead of waiting for texts that will never be translated.
            mRunning = false;
            status("Este equipo no tiene servicios de Google Play: el traductor del teléfono no puede funcionar. Usa DeepL en Ajustes → Traducción.");
            return;
        }
        Thread t = new Thread(this::loop, "gahvoria-translate");
        t.setDaemon(true);
        t.start();
        status("Esperando texto del juego");
    }

    public void stop()
    {
        mRunning = false;
        try { if (mTranslator != null) mTranslator.close(); } catch (Exception ignored) { }
    }

    // ---------------------------------------------------------------- loop

    private void loop()
    {
        while (mRunning) {
            try {
                File[] files = mReqDir.listFiles((d, n) -> n.endsWith(".req"));
                if (files == null || files.length == 0) { Thread.sleep(150); continue; }
                Arrays.sort(files);
                List<String> todo = new ArrayList<>();
                for (File f : files) {
                    for (String line : readLines(f)) {
                        String text = unescape(line);
                        if (!text.isEmpty() && !mKnown.contains(text) && !todo.contains(text)) todo.add(text);
                    }
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
                if (todo.isEmpty()) continue;
                translateAll(todo);
            } catch (InterruptedException e) {
                return;
            } catch (Throwable e) {
                Log.w(TAG, "Translation round failed", e);
                status(friendly(e));
                try { Thread.sleep(2000); } catch (InterruptedException ie) { return; }
            }
        }
    }

    private void translateAll(List<String> texts) throws Exception
    {
        List<String> out = new ArrayList<>(texts.size());
        // Control codes (\c[1], \PN, <b>...) are swapped for markers and put back; if one goes missing the text is left alone.
        List<List<String>> codes = new ArrayList<>();
        List<String> shielded = new ArrayList<>();
        for (String t : texts) {
            List<String> c = new ArrayList<>();
            shielded.add(shield(t, c));
            codes.add(c);
        }
        List<String> translated = PROVIDER_DEEPL.equals(mProvider) ? deepl(shielded) : mlkit(shielded);
        for (int i = 0; i < texts.size(); i++) {
            String r = unshield(translated.get(i), codes.get(i));
            out.add(r == null || r.trim().isEmpty() ? texts.get(i) : r);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texts.size(); i++) {
            sb.append(escape(texts.get(i))).append('\t').append(escape(out.get(i))).append('\n');
            mKnown.add(texts.get(i));
        }
        try (OutputStream os = new FileOutputStream(mFile, true)) {
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        mDone += texts.size();
        status("Traducidas " + mDone + " frases");
    }

    // ---------------------------------------------------------------- ML Kit

    private List<String> mlkit(List<String> texts) throws Exception
    {
        String source = chooseSource(texts);
        List<String> out = new ArrayList<>();
        String sourceTag = TranslateLanguage.fromLanguageTag(source);
        String targetTag = TranslateLanguage.fromLanguageTag(mTarget);
        if (targetTag == null) throw new IllegalStateException("Idioma de destino no disponible: " + mTarget);
        if (sourceTag == null || sourceTag.equals(targetTag)) {
            out.addAll(texts);  // already in the target language (or not a language ML Kit knows): leave as it is
            return out;
        }
        if (mTranslator == null || !sourceTag.equals(mTranslatorSource)) {
            if (mTranslator != null) mTranslator.close();
            mTranslator = Translation.getClient(new TranslatorOptions.Builder()
                .setSourceLanguage(sourceTag).setTargetLanguage(targetTag).build());
            mTranslatorSource = sourceTag;
            status("Descargando el idioma (" + sourceTag + " → " + targetTag + "), solo la primera vez");
            Tasks.await(mTranslator.downloadModelIfNeeded(new DownloadConditions.Builder().build()), 180, TimeUnit.SECONDS);
        }
        for (String t : texts) {
            Task<String> task = mTranslator.translate(t);
            out.add(Tasks.await(task, 20, TimeUnit.SECONDS));
        }
        return out;
    }

    /** The game's language: found from the first texts, then kept once three in a row agree. */
    private String chooseSource(List<String> texts) throws Exception
    {
        if (!mLockedSource.isEmpty()) return mLockedSource;
        if (mIdentifier == null) mIdentifier = LanguageIdentification.getClient();
        for (String t : texts) {
            if (t.length() < 12) continue;
            String tag = Tasks.await(mIdentifier.identifyLanguage(t), 10, TimeUnit.SECONDS);
            if ("und".equals(tag)) continue;
            if (tag.equals(mLastVote)) mVotes++; else { mLastVote = tag; mVotes = 1; }
            if (mVotes >= 3) { mLockedSource = tag; return tag; }
        }
        return mLastVote.isEmpty() ? "en" : mLastVote;
    }

    // ---------------------------------------------------------------- DeepL

    private List<String> deepl(List<String> texts) throws Exception
    {
        if (mKey.isEmpty()) throw new IllegalStateException("Falta la clave de DeepL");
        List<String> out = new ArrayList<>();
        for (int from = 0; from < texts.size(); from += 40) {
            List<String> part = texts.subList(from, Math.min(texts.size(), from + 40));
            JSONObject body = new JSONObject();
            body.put("text", new JSONArray(part));
            body.put("target_lang", deeplTarget(mTarget));
            body.put("preserve_formatting", true);
            String host = mKey.endsWith(":fx") ? "https://api-free.deepl.com" : "https://api.deepl.com";
            HttpURLConnection c = (HttpURLConnection) new URL(host + "/v2/translate").openConnection();
            c.setConnectTimeout(10000);
            c.setReadTimeout(20000);
            c.setRequestMethod("POST");
            c.setRequestProperty("Authorization", "DeepL-Auth-Key " + mKey);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) { os.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
            int code = c.getResponseCode();
            if (code == 403) throw new IllegalStateException("DeepL rechazó la clave");
            if (code == 456) throw new IllegalStateException("Se acabó la cuota de DeepL");
            if (code / 100 != 2) throw new IllegalStateException("DeepL respondió " + code);
            String text;
            try (InputStream is = c.getInputStream()) { text = readAll(is); }
            JSONArray arr = new JSONObject(text).getJSONArray("translations");
            for (int i = 0; i < arr.length(); i++) out.add(arr.getJSONObject(i).getString("text"));
            if (arr.length() != part.size()) throw new IllegalStateException("DeepL devolvió otra cantidad de textos");
        }
        return out;
    }

    private static String deeplTarget(String tag)
    {
        switch (tag) {
            case "en": return "EN-US";
            case "pt": return "PT-BR";
            default: return tag.toUpperCase(Locale.ROOT);
        }
    }

    // ---------------------------------------------------------------- control codes

    private static final Pattern CODES = Pattern.compile("\\\\[A-Za-z]+(?:\\[[^\\]]*\\])?|\\\\[.!|^<>{}$\\\\]|<[^>\\n]{1,40}>|\\[[A-Za-z_]+\\]");

    static String shield(String text, List<String> codes)
    {
        Matcher m = CODES.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            codes.add(m.group());
            m.appendReplacement(sb, Matcher.quoteReplacement("⟦" + (codes.size() - 1) + "⟧"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Puts the control codes back. Null when one is missing, so the original text is kept instead. */
    static String unshield(String text, List<String> codes)
    {
        if (text == null) return null;
        String r = text;
        for (int i = 0; i < codes.size(); i++) {
            String marker = "⟦" + i + "⟧";
            if (!r.contains(marker)) return null;
            r = r.replace(marker, codes.get(i));
        }
        return r.contains("⟦") ? null : r;
    }

    // ---------------------------------------------------------------- files

    private void loadKnown()
    {
        if (!mFile.isFile()) return;
        try {
            for (String line : readLines(mFile)) {
                int tab = line.indexOf('\t');
                if (tab > 0) mKnown.add(unescape(line.substring(0, tab)));
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not read the saved translations", e);
        }
    }

    private static List<String> readLines(File f) throws Exception
    {
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) if (!l.isEmpty()) lines.add(l);
        }
        return lines;
    }

    private static String readAll(InputStream is) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    static String escape(String s)
    {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
    }

    static String unescape(String s)
    {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 'n' ? '\n' : n == 't' ? '\t' : n == 'r' ? '\r' : n);
            } else sb.append(c);
        }
        return sb.toString();
    }

    /** A failure in words the player can act on. */
    private static String friendly(Throwable e)
    {
        String all = e.getClass().getSimpleName() + " " + e.getMessage() + " " + (e.getCause() == null ? "" : e.getCause().getMessage());
        String low = all.toLowerCase(Locale.ROOT);
        if (low.contains("mlkitexception") || low.contains("google play") || low.contains("gms"))
            return "El traductor del teléfono falló (necesita servicios de Google Play y conexión la primera vez). Si no funciona, usa DeepL.";
        if (low.contains("timeout") || low.contains("unknownhost") || low.contains("unable to resolve"))
            return "Sin conexión: no pude traducir. Las frases ya traducidas siguen funcionando.";
        return "Error: " + shorten(e);
    }

    private static String shorten(Throwable e)
    {
        String m = e.getMessage();
        String s = e.getClass().getSimpleName() + (m == null ? "" : ": " + m);
        return s.length() > 140 ? s.substring(0, 140) : s;
    }

    /** What the app shows in Ajustes: the last thing this translator did. */
    private void status(String message)
    {
        mMessage = message;
        try (OutputStream os = new FileOutputStream(mStatus)) {
            os.write((mDone + "\n" + mMessage + "\n" + System.currentTimeMillis() + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { }
    }
}
