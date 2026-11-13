package com.hatkid.mkxpz.translate;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.MlKit;
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Processes the Ruby game's private mailbox in the background. Durable memory stays per game and target;
 * Ruby receives validated answers through a disposable session file.
 */
public final class TranslatorService
{
    private static final String TAG = "GahvoriaTranslate";
    public static final String PROVIDER_MLKIT = "mlkit";
    public static final String PROVIDER_DEEPL = "deepl";
    private static final String PROVIDER_NEURAL = "neural";

    private final File mReqDir;
    private final File mFile;
    private final File mResponses;
    private final File mStatus;
    private final File mPreparation;
    private final File mPreparedCatalog;
    public interface PreparationProgress { void show(String message); }

    private final PreparationProgress mProgress;
    private final String mProvider;
    private final String mTarget;
    private final String mKey;
    private Thread mWorker;
    private volatile boolean mRunning;
    private int mDone;

    // ML Kit has a separate runtime in the game's Android process.
    private static boolean sMlKitInitialized;

    // ML Kit client state (only used by the thread)
    private final Context mContext;
    private BergamotModel mBergamot;
    private boolean mNeuralUnavailable;
    private Translator mTranslator;
    private String mTranslatorSource = "";
    private Task<Void> mModelDownload;
    private LanguageIdentifier mIdentifier;
    private String mLockedSource = "";
    private String mLastVote = "";
    private int mVotes;

    public TranslatorService(Context context, File dir, String provider, String target, String key, PreparationProgress progress)
    {
        mContext = context.getApplicationContext();
        String tag = target == null ? "" : target.toLowerCase(Locale.ROOT);
        mTarget = tag.matches("[a-z]{2,3}(-[a-z0-9]{2,8}){0,3}") ? tag : "es";
        mFile = new File(dir, mTarget + ".tsv");
        mPreparedCatalog = new File(dir, "catalog.ready");
        File session = new File(context.getCacheDir(), "translation-session");
        mReqDir = new File(session, "queue");
        mResponses = new File(session, "answers.tsv");
        mStatus = new File(context.getFilesDir(), "translate_status.txt");
        mPreparation = new File(session, "prepare");
        mProgress = progress;
        mProvider = provider == null ? PROVIDER_MLKIT : provider;
        mKey = key == null ? "" : key.trim();
    }

    public File translationsFile() { return mResponses; }
    public File requestDir() { return mReqDir; }

    public String preparationPath() {
        return PROVIDER_NEURAL.equals(mProvider) && "es".equals(mTarget) ? mPreparation.getAbsolutePath() : "";
    }

    private File preparationFile(String suffix) {
        return new File(mPreparation.getPath() + suffix);
    }

    public void skipPreparation() {
        try {
            preparationFile(".skip").createNewFile();
        } catch (IOException failure) {
            Log.w(TAG, "Could not skip translation preparation", failure);
        }
    }

    public void start() throws IOException
    {
        if (!mReqDir.mkdirs() && !mReqDir.isDirectory()) throw new IOException("No se pudo abrir el buzón");
        File[] stale = mReqDir.listFiles();
        if (stale == null) throw new IOException("No se pudo leer el buzón");
        for (File entry : stale) {
            if ((entry.getName().endsWith(".req") || entry.getName().endsWith(".tmp")) && !entry.delete()) {
                throw new IOException("No se pudo limpiar el buzón");
            }
        }
        for (String suffix : new String[]{".tmp", ".ready", ".done", ".skip"}) {
            File file = preparationFile(suffix);
            if (file.exists() && !file.delete()) throw new IOException("No se pudo preparar el buzón de traducción");
        }
        try (RandomAccessFile responses = new RandomAccessFile(mResponses, "rw")) { responses.setLength(0); }
        mRunning = true;
        status("Esperando texto del juego");
        mWorker = new Thread(this::loop, "gahvoria-translate");
        mWorker.setDaemon(true);
        mWorker.start();
    }

    public void stop()
    {
        mRunning = false;
        if (mWorker != null) mWorker.interrupt();
    }

    private void loop()
    {
        try (TranslationMemory memory = new TranslationMemory(mFile, PROVIDER_NEURAL.equals(mProvider) && "es".equals(mTarget));
             RandomAccessFile responses = new RandomAccessFile(mResponses, "rw")) {
            seedPreparedTranslations(memory, responses);
            Map<File, Long> retryAfter = new LinkedHashMap<>();
            Map<File, Long> firstFailures = new LinkedHashMap<>();
            boolean scanning = false;
            boolean preparationHandled = preparationPath().isEmpty();
            while (mRunning) {
                if (!preparationHandled && preparationFile(".ready").isFile()) {
                    preparationHandled = true;
                    prepare(memory, responses);
                    scanning = false;
                } else if (!preparationHandled && preparationFile(".skip").exists()) {
                    preparationHandled = true;
                    if (scanning) mProgress.show(null);
                } else if (!preparationHandled && !scanning && preparationFile(".tmp").isFile()) {
                    scanning = true;
                    mProgress.show("Buscando textos del juego…");
                }
                File[] files = mReqDir.listFiles((d, name) -> name.endsWith(".req"));
                if (files == null || files.length == 0) {
                    Thread.sleep(120);
                    continue;
                }
                Arrays.sort(files);
                long now = android.os.SystemClock.elapsedRealtime();
                File request = null;
                for (File file : files) {
                    Long retry = retryAfter.get(file);
                    if (retry == null || retry <= now) { request = file; break; }
                }
                if (request == null) { Thread.sleep(120); continue; }
                try {
                    answer(request, memory, responses);
                    if (!request.delete() && request.exists()) throw new IOException("No se pudo cerrar la solicitud");
                    retryAfter.remove(request);
                    firstFailures.remove(request);
                } catch (InterruptedException stop) {
                    throw stop;
                } catch (Exception failure) {
                    if (!"Preparando el idioma".equals(failure.getMessage())) Log.w(TAG, "Translation round failed", failure);
                    status(friendly(failure));
                    Long previousFailure = firstFailures.get(request);
                    long firstFailure = previousFailure == null ? now : previousFailure;
                    if (now - firstFailure >= 120_000 && (request.delete() || !request.exists())) {
                        firstFailures.remove(request);
                        retryAfter.remove(request);
                    } else {
                        firstFailures.put(request, firstFailure);
                        retryAfter.put(request, android.os.SystemClock.elapsedRealtime() + 5000);
                    }
                }
            }
        } catch (InterruptedException stop) {
            Thread.currentThread().interrupt();
        } catch (Exception failure) {
            Log.w(TAG, "Translation storage unavailable", failure);
            status(friendly(failure));
        } finally {
            try {
                if (mTranslator != null) mTranslator.close();
            } finally {
                try {
                    if (mIdentifier != null) mIdentifier.close();
                } finally {
                    if (mBergamot != null) mBergamot.close();
                }
            }
        }
    }

    /** Make cached opening text available before the first round trip through the mailbox. */
    private void seedPreparedTranslations(TranslationMemory memory, RandomAccessFile responses) {
        if (!"es".equals(mTarget) || !mPreparedCatalog.isFile()) return;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(
                new FileInputStream(mPreparedCatalog), StandardCharsets.UTF_8))) {
            Map<String, String> cached = new LinkedHashMap<>();
            int chars = 0;
            for (int i = 0; i < 384 && chars < 96_000 && mRunning; i++) {
                String line = input.readLine();
                if (line == null) break;
                String source = TranslationMemory.unescape(line);
                if (source == null) continue;
                String answer = memory.get(source);
                if (answer != null) {
                    cached.put(source, answer);
                    chars += source.length() + answer.length();
                }
            }
            TranslationMemory.append(responses, cached, false);
        } catch (IOException failure) {
            Log.w(TAG, "Could not preload cached translations", failure);
        }
    }

    private void prepare(TranslationMemory memory, RandomAccessFile responses) {
        File ready = preparationFile(".ready");
        File skip = preparationFile(".skip");
        int processed = 0;
        try {
            if (skip.exists()) return;
            mProgress.show("Preparando traducción del juego…");
            int total = 0;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(ready), StandardCharsets.UTF_8))) {
                while (mRunning && !skip.exists() && input.readLine() != null) total++;
            }
            long lastUpdate = 0;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(ready), StandardCharsets.UTF_8))) {
                List<String> batch = new ArrayList<>(8);
                String line;
                while (mRunning && !skip.exists() && (line = input.readLine()) != null) {
                    String text = TranslationMemory.unescape(line);
                    if (text == null || text.isEmpty()) continue;
                    batch.add(text);
                    if (batch.size() < 8) continue;
                    prepareBatch(batch, memory, responses);
                    processed += batch.size();
                    batch.clear();
                    long now = android.os.SystemClock.elapsedRealtime();
                    if (now - lastUpdate >= 400) {
                        reportPreparation(processed, total);
                        lastUpdate = now;
                    }
                }
                if (mRunning && !skip.exists() && !batch.isEmpty()) {
                    prepareBatch(batch, memory, responses);
                    processed += batch.size();
                }
            }
            status((skip.exists() ? "Preparación parcial: " : "Preparación: ") + processed + " frases revisadas");
        } catch (Exception | LinkageError failure) {
            Log.w(TAG, "Could not prepare game translations", failure);
            status("Preparación incompleta. " + friendly(failure));
        } finally {
            try {
                preparationFile(".done").createNewFile();
                ready.delete();
            } catch (IOException failure) {
                Log.w(TAG, "Could not close translation preparation", failure);
            }
            mProgress.show(null);
        }
    }

    private void prepareBatch(List<String> batch, TranslationMemory memory, RandomAccessFile responses) throws Exception {
        Map<String, String> answers = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (String text : batch) {
            String cached = memory.get(text);
            if (cached == null) missing.add(text);
            else answers.put(text, cached);
        }
        if (!missing.isEmpty()) {
            Map<String, String> translated = TranslationBatch.translate(missing, this::neural);
            memory.remember(translated);
            for (Map.Entry<String, String> entry : translated.entrySet()) {
                if (!entry.getKey().equals(entry.getValue())) answers.put(entry.getKey(), entry.getValue());
            }
        }
        TranslationMemory.append(responses, answers, false);
    }

    private void reportPreparation(int processed, int total) {
        String message = "Preparando traducción: " + processed + " / " + total + " frases";
        mProgress.show(message);
    }

    private void answer(File request, TranslationMemory memory, RandomAccessFile responses) throws Exception
    {
        // Ruby publishes one complete file with at most eight phrases.
        if (request.length() > 160_000) return;
        LinkedHashSet<String> texts = new LinkedHashSet<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(request), StandardCharsets.UTF_8))) {
            String line;
            while (texts.size() < 8 && (line = input.readLine()) != null) {
                String original = TranslationMemory.unescape(line);
                if (original != null && !original.isEmpty() && original.getBytes(StandardCharsets.UTF_8).length <= 9000) {
                    texts.add(original);
                }
            }
        }
        Map<String, String> cached = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (String original : texts) {
            String translation = memory.get(original);
            if (translation == null) missing.add(original);
            else cached.put(original, translation);
        }
        TranslationMemory.append(responses, cached, false);
        if (missing.isEmpty()) {
            if (!cached.isEmpty()) status("Usando la memoria de traducción del juego");
            return;
        }
        if (PROVIDER_NEURAL.equals(mProvider) && !"es".equals(mTarget)) {
            status("Gahvoria Neural AI traduce del inglés al español. Elige Español como destino.");
            return;
        }
        if (PROVIDER_NEURAL.equals(mProvider) && mNeuralUnavailable) return;
        if (PROVIDER_DEEPL.equals(mProvider) && mKey.isEmpty()) {
            status("Falta la clave de DeepL. La memoria del juego sigue disponible.");
            return;
        }

        TranslationBatch.Provider provider;
        if (PROVIDER_NEURAL.equals(mProvider)) {
            provider = this::neural;
        } else if (PROVIDER_DEEPL.equals(mProvider)) {
            provider = (originals, protectedTexts) -> deepl(protectedTexts);
        } else {
            provider = this::mlkit;
        }
        Map<String, String> answers = TranslationBatch.translate(missing, provider);
        memory.remember(answers);
        TranslationMemory.append(responses, answers, false);
        int translated = 0;
        for (Map.Entry<String, String> answer : answers.entrySet()) {
            if (!answer.getKey().equals(answer.getValue())) translated++;
        }
        mDone += translated;
        if (translated > 0) {
            status("Traducidas " + mDone + " frases");
        } else if (answers.isEmpty()) {
            status("Recibí texto del juego, pero no salió una traducción válida");
        } else {
            status("Recibí texto del juego, pero quedó en el idioma original");
        }
    }

    private List<String> neural(List<String> originals, List<String> protectedTexts) throws Exception
    {
        if (mBergamot == null) {
            status("Cargando el modelo Neural inglés → español");
            try {
                mBergamot = new BergamotModel(mContext);
            } catch (Exception | LinkageError failure) {
                mNeuralUnavailable = true;
                if (failure instanceof Exception) throw (Exception) failure;
                throw new IOException("No pude cargar el motor Neural", failure);
            }
        }
        return NeuralText.translate(protectedTexts, mBergamot::translate, mBergamot::translateMarkup);
    }

    // ---------------------------------------------------------------- ML Kit

    // ML Kit's default initializer only runs in the main process. GameActivity runs in :game.
    private static synchronized void initializeMlKit(Context context)
    {
        if (sMlKitInitialized) return;
        MlKit.initialize(context);
        sMlKitInitialized = true;
    }

    private List<String> mlkit(List<String> originals, List<String> texts) throws Exception
    {
        try {
            initializeMlKit(mContext);
        } catch (RuntimeException failure) {
            throw new IOException("No se pudo iniciar el traductor del teléfono", failure);
        }
        String source = chooseSource(originals);
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
            mModelDownload = null;
        }
        if (mModelDownload == null) {
            mModelDownload = mTranslator.downloadModelIfNeeded(new DownloadConditions.Builder().build());
        }
        if (!mModelDownload.isComplete()) {
            throw new IOException("Preparando el idioma");
        }
        if (!mModelDownload.isSuccessful()) {
            Exception failure = mModelDownload.getException();
            mModelDownload = null;
            throw new IOException("No se pudo descargar el modelo de idioma", failure);
        }
        int failures = 0;
        for (String t : texts) {
            try {
                Task<String> task = mTranslator.translate(t);
                out.add(Tasks.await(task, 5, TimeUnit.SECONDS));
            } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException failure) {
                Log.w(TAG, "ML Kit could not translate a phrase", failure);
                out.add(null);
                failures++;
                mModelDownload = null;
            }
        }
        if (failures == texts.size()) throw new IOException("El modelo de idioma no pudo traducir el texto. Reintentando.");
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
        List<String> out = new ArrayList<>();
        for (int from = 0; from < texts.size();) {
            int end = from;
            JSONObject body = new JSONObject();
            body.put("target_lang", deeplTarget(mTarget));
            byte[] payload;
            do {
                end++;
                body.put("text", new JSONArray(texts.subList(from, end)));
                payload = body.toString().getBytes(StandardCharsets.UTF_8);
                if (payload.length > 120_000) {
                    if (end - from == 1) throw new IOException("La frase supera el límite de DeepL");
                    end--;
                    body.put("text", new JSONArray(texts.subList(from, end)));
                    payload = body.toString().getBytes(StandardCharsets.UTF_8);
                    break;
                }
            } while (end < texts.size());
            String host = mKey.endsWith(":fx") ? "https://api-free.deepl.com" : "https://api.deepl.com";
            HttpURLConnection c = (HttpURLConnection) new URL(host + "/v2/translate").openConnection();
            try {
                c.setConnectTimeout(10000);
                c.setReadTimeout(20000);
                c.setRequestMethod("POST");
                c.setRequestProperty("Authorization", "DeepL-Auth-Key " + mKey);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                c.setDoOutput(true);
                c.setFixedLengthStreamingMode(payload.length);
                try (OutputStream os = c.getOutputStream()) { os.write(payload); }
                int code = c.getResponseCode();
                if (code == 403) throw new IllegalStateException("DeepL rechazó la clave");
                if (code == 456) throw new IllegalStateException("Se acabó la cuota de DeepL");
                if (code / 100 != 2) throw new IllegalStateException("DeepL respondió " + code);
                String response;
                try (InputStream is = c.getInputStream()) { response = readAll(is); }
                JSONArray translated = new JSONObject(response).getJSONArray("translations");
                if (translated.length() != end - from) throw new IOException("DeepL devolvió otra cantidad de textos");
                for (int i = 0; i < translated.length(); i++) out.add(translated.getJSONObject(i).getString("text"));
            } finally {
                c.disconnect();
            }
            from = end;
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

    /** A failure in words the player can act on. */
    private static String friendly(Throwable e)
    {
        String all = e.getClass().getSimpleName() + " " + e.getMessage() + " " + (e.getCause() == null ? "" : e.getCause().getMessage());
        String low = all.toLowerCase(Locale.ROOT);
        if (low.contains("preparando el idioma")) return "Preparando el idioma. La primera descarga necesita conexión.";
        if ("No se pudo iniciar el traductor del teléfono".equals(e.getMessage())) return e.getMessage();
        if ("No se pudo descargar el modelo de idioma".equals(e.getMessage()))
            return "No pude descargar el modelo de idioma. Revisa la conexión y vuelve a abrir el juego.";
        if (low.contains("el modelo de idioma no pudo traducir")) return e.getMessage();
        if (low.contains("mlkitexception") || low.contains("google play") || low.contains("gms"))
            return "El traductor del teléfono falló. Revisa la conexión para descargar el modelo de idioma.";
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
        File temporary = new File(mStatus.getParentFile(), mStatus.getName() + ".tmp");
        try {
            try (OutputStream os = new FileOutputStream(temporary)) {
                os.write((mDone + "\n" + message.replace('\n', ' ').replace('\r', ' ') + "\n"
                    + System.currentTimeMillis() + "\n").getBytes(StandardCharsets.UTF_8));
            }
            if (!temporary.renameTo(mStatus)) Log.w(TAG, "Could not publish translator status");
        } catch (IOException failure) {
            Log.w(TAG, "Could not save translator status", failure);
        }
    }
}
