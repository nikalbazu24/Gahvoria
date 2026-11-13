package com.hatkid.mkxpz;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hatkid.mkxpz.gamepad.Gamepad;
import com.hatkid.mkxpz.gamepad.GamepadConfig;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.FutureTask;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Gahvoria's game screen: hosts the mkxp-z engine (modern Ruby 3.1 or classic Ruby 1.8).
 *
 * The app starts it with {@link #intent}. The native engine reads {@code GAME_PATH} and calls
 * the static methods below by name (see mkxp-z/src), so their names and signatures must not change.
 * It runs in its own process (":game"): Ruby cannot start twice in one process, so the process
 * ends with the game.
 */
public class GameActivity extends SDLActivity
{
    private static final String TAG = "Gahvoria[Game]";

    public static final String EXTRA_GAME_PATH = "gahvoria.GAME_PATH";
    public static final String EXTRA_TITLE = "gahvoria.TITLE";
    public static final String EXTRA_SAVE_DIR = "gahvoria.SAVE_DIR";
    public static final String EXTRA_ENGINE = "gahvoria.ENGINE";
    public static final String EXTRA_SCREEN = "gahvoria.SCREEN";
    public static final String EXTRA_RTP_ROOT = "gahvoria.RTP_ROOT";
    public static final String EXTRA_TOUCH = "gahvoria.TOUCH";
    /** Short line under the title on the loading screen ("Essentials 17.2 · motor clásico"). */
    public static final String EXTRA_INFO = "gahvoria.INFO";
    /** The palette in use: bg, bg2, ink, muted, accent, accent2, accent3, onAccent, ok, warn (ARGB ints). */
    public static final String EXTRA_COLORS = "gahvoria.COLORS";
    /** The player's arrangement of the touch controls ("dpad,0.05,-0.02,1.2,0;..."), empty for the default. */
    public static final String EXTRA_PAD = "gahvoria.PAD";
    /** A small copy of the game's cover, for the blurred background of the loading screen ("" if none). */
    public static final String EXTRA_COVER = "gahvoria.COVER";
    /** Translation: the folder that keeps this game's translations, the provider, the target language and the DeepL key. */
    public static final String EXTRA_TRANSLATION_CATALOG = "gahvoria.TRANSLATION_CATALOG";
    public static final String EXTRA_LAUNCH_AT = "gahvoria.LAUNCH_AT";
    public static final String EXTRA_SKIP_TR_PREPARATION = "gahvoria.SKIP_TR_PREPARATION";
    public static final String EXTRA_TR_DIR = "gahvoria.TR_DIR";
    public static final String EXTRA_TR_PROVIDER = "gahvoria.TR_PROVIDER";
    public static final String EXTRA_TR_TARGET = "gahvoria.TR_TARGET";
    public static final String EXTRA_TR_KEY = "gahvoria.TR_KEY";
    /** Picture style ("crt" = old TV lines) and the wide-screen mode value. */
    public static final String EXTRA_FILTER = "gahvoria.FILTER";
    /** Lean-back angle in degrees (0 to 60) of the experimental 2.5D / 3D effect; 0 = flat. */
    public static final String EXTRA_TILT = "gahvoria.TILT";

    public static final String ENGINE_MODERN = "modern";
    public static final String ENGINE_CLASSIC = "classic";
    public static final String SCREEN_ORIGINAL = "original";
    /** Wide only on the map; menus and battles keep their own shape in the middle. */
    public static final String SCREEN_WIDE = "wide_safe";
    /** Wide everywhere, battles included. */
    public static final String SCREEN_WIDE_FULL = "wide";

    /** Read by the native engine through JNI (main.cpp). */
    @SuppressWarnings("unused")
    private static String GAME_PATH = "";

    private static Handler sMain;
    private static Vibrator sVibrator;
    private static TextView sFps;
    private static LoadingScreen sScreen;

    private boolean mClassic;
    private boolean mTouch = true;
    private final Gamepad mGamepad = new Gamepad();
    private boolean mPadHidden;
    private TextView mKeyboardButton;
    private FutureTask<File> mTrust;
    private Dialog mExit;
    private Look mLook;
    private String mTitle = "";

    /** Builds the intent that opens a game. */
    public static Intent intent(Context c, File game, String title, File saveDir, String engine,
                                String screen, File rtpRoot, boolean touch, String info, int[] colors)
    {
        return new Intent(c, GameActivity.class)
            .putExtra(EXTRA_GAME_PATH, game.getAbsolutePath())
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_SAVE_DIR, saveDir.getAbsolutePath())
            .putExtra(EXTRA_ENGINE, engine)
            .putExtra(EXTRA_SCREEN, screen)
            .putExtra(EXTRA_RTP_ROOT, rtpRoot == null ? "" : rtpRoot.getAbsolutePath())
            .putExtra(EXTRA_TOUCH, touch)
            .putExtra(EXTRA_INFO, info == null ? "" : info)
            .putExtra(EXTRA_COLORS, colors);
    }

    /** True when this APK carries the classic engine (Ruby 1.8). */
    public static boolean classicAvailable(Context c)
    {
        return new File(c.getApplicationInfo().nativeLibraryDir, "libmkxp-z-legacy.so").isFile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        long loadingStarted = SystemClock.elapsedRealtime();
        Intent i = getIntent();
        mLook = new Look(this, i == null ? null : i.getIntArrayExtra(EXTRA_COLORS));
        String path = i == null ? null : i.getStringExtra(EXTRA_GAME_PATH);
        String givenTitle = i == null ? null : i.getStringExtra(EXTRA_TITLE);
        mTitle = givenTitle == null ? "" : givenTitle;
        boolean valid = path != null && new File(path).isDirectory();
        if (valid) GAME_PATH = path;
        mClassic = i != null && ENGINE_CLASSIC.equals(i.getStringExtra(EXTRA_ENGINE)) && classicAvailable(this);
        mTouch = i == null || i.getBooleanExtra(EXTRA_TOUCH, true);
        sMain = new Handler(Looper.getMainLooper());
        sVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (valid) {
            String catalog = i.getStringExtra(EXTRA_TRANSLATION_CATALOG);
            if (catalog == null) {
                mTrust = new FutureTask<>(() -> trustFile(getApplicationContext()));
                new Thread(mTrust, "GahvoriaTrust").start();
                prepareEnvironment(new File(path), i);
            } else {
                try {
                    setenv("ESSENTIA_TR_ROOT", path);
                    setenv("ESSENTIA_TR_CATALOG", catalog);
                    setenv("ESSENTIA_TR_TARGET", "es");
                } catch (Exception failure) {
                    Log.w(TAG, "Could not configure translation scan", failure);
                    super.onCreate(savedInstanceState);
                    finish();
                    return;
                }
            }
        }

        super.onCreate(savedInstanceState);
        if (!valid) {
            Log.e(TAG, "No game folder: " + path);
            finish();
            return;
        }
        if (mLayout == null) return;

        GamepadConfig config = new GamepadConfig();
        mGamepad.init(config, !mTouch);
        mGamepad.setOnKeyDownListener(k -> SDLActivity.onNativeKeyDown(k));
        mGamepad.setOnKeyUpListener(k -> SDLActivity.onNativeKeyUp(k));
        mGamepad.setLayout(i == null ? null : i.getStringExtra(EXTRA_PAD));
        if (mTouch) mGamepad.attachTo(this, mLayout);
        mPadHidden = !mTouch;

        sFps = new TextView(this);
        sFps.setTextColor(Color.argb(150, 255, 255, 255));
        sFps.setTextSize(12);
        sFps.setPadding(24, 16, 0, 0);
        sFps.setVisibility(View.GONE);
        mLayout.addView(sFps, new RelativeLayout.LayoutParams(-2, -2));

        if (mTouch) addKeyboardButton();

        sScreen = new LoadingScreen(this, mLook, mTitle, i.getStringExtra(EXTRA_INFO), i.getStringExtra(EXTRA_COVER), loadingStarted,
            mPreparesTranslation, new LoadingScreen.Host()
        {
            @Override
            public void exit()
            {
                if (i.hasExtra(EXTRA_TRANSLATION_CATALOG)) {
                    try { new File(i.getStringExtra(EXTRA_TRANSLATION_CATALOG) + ".skip").createNewFile(); }
                    catch (IOException failure) { Log.w(TAG, "Could not pause catalog scan", failure); }
                }
                if (mPreparesTranslation) {
                    mTranslator.skipPreparation();
                    translationPreparationMarker(GameActivity.this, i.getLongExtra(EXTRA_LAUNCH_AT, 0L)).delete();
                }
                finish();
            }

            @Override
            public void copyReport(long[] times, long totalMs)
            {
                copyText(buildReport(times, totalMs));
                notice("Informe copiado. Pégalo donde quieras enviarlo.", mLook.ok);
            }

            @Override
            public void loaded(long[] times, long totalMs)
            {
                saveLoadTimes(times, totalMs);
            }
        });
        mLayout.addView(sScreen, new RelativeLayout.LayoutParams(-1, -1));
        if (i.hasExtra(EXTRA_TRANSLATION_CATALOG)) {
            sScreen.setTranslationPreparation("Leyendo mapas, eventos y scripts…", "Pausar lectura", this::finish);
        }
    }

    private com.hatkid.mkxpz.translate.TranslatorService mTranslator;
    private boolean mPreparesTranslation;

    /** When this game is set to be translated, tells the Ruby side where the translations live and starts the translator. */
    private void startTranslator(Intent i) throws Exception
    {
        setenv("ESSENTIA_TR_FILE", "");
        setenv("ESSENTIA_TR_REQ", "");
        setenv("ESSENTIA_TR_OPENING", "");
        setenv("ESSENTIA_TR_TARGET", "");
        setenv("ESSENTIA_TR_GAME", "");
        setenv("ESSENTIA_TR_PREPARE", "");
        setenv("ESSENTIA_TR_ROOT", "");
        setenv("ESSENTIA_TR_PREPARE_MARKER", "");
        String dir = i.getStringExtra(EXTRA_TR_DIR);
        if (dir == null || dir.isEmpty()) return;
        File folder = new File(dir);
        if (!folder.mkdirs() && !folder.isDirectory()) {
            Log.w(TAG, "Could not create the translation directory");
            return;
        }
        mTranslator = new com.hatkid.mkxpz.translate.TranslatorService(this, folder,
            i.getStringExtra(EXTRA_TR_PROVIDER), i.getStringExtra(EXTRA_TR_TARGET), i.getStringExtra(EXTRA_TR_KEY),
            message -> sMain.post(() -> {
                if (sScreen != null) sScreen.setTranslationPreparation(message, mTranslator::skipPreparation);
            }));
        try {
            mTranslator.start();
            setenv("ESSENTIA_TR_FILE", mTranslator.translationsFile().getAbsolutePath());
            setenv("ESSENTIA_TR_REQ", mTranslator.requestDir().getAbsolutePath());
            setenv("ESSENTIA_TR_OPENING", mTranslator.openingPath());
            String target = i.getStringExtra(EXTRA_TR_TARGET);
            setenv("ESSENTIA_TR_TARGET", target == null ? "es" : target);
            setenv("ESSENTIA_TR_GAME", mTitle);
            String preparation = i.getBooleanExtra(EXTRA_SKIP_TR_PREPARATION, false) ? "" : mTranslator.preparationPath();
            if (!preparation.isEmpty()) {
                long launchAt = i.getLongExtra(EXTRA_LAUNCH_AT, 0L);
                if (launchAt > 0L) {
                    setenv("ESSENTIA_TR_PREPARE_MARKER", translationPreparationMarker(this, launchAt).getAbsolutePath());
                }
                setenv("ESSENTIA_TR_PREPARE", preparation);
                mPreparesTranslation = true;
            }
            String root = i.getStringExtra(EXTRA_GAME_PATH);
            setenv("ESSENTIA_TR_ROOT", root == null ? "" : root);
        } catch (Exception failure) {
            mTranslator.stop();
            setenv("ESSENTIA_TR_FILE", "");
            setenv("ESSENTIA_TR_REQ", "");
            setenv("ESSENTIA_TR_PREPARE", "");
            Log.w(TAG, "Could not start the optional translator", failure);
        }
    }

    public static File translationPreparationMarker(android.content.Context context, long launchAt) {
        return new File(context.getCacheDir(), "translation-preparing-" + launchAt);
    }

    /** Settings the engine and its compatibility scripts read from the environment. */
    private void prepareEnvironment(File game, Intent i)
    {
        try {
            String save = i.getStringExtra(EXTRA_SAVE_DIR);
            File saveDir = save == null || save.isEmpty() ? game : new File(save);
            //noinspection ResultOfMethodCallIgnored
            saveDir.mkdirs();
            setenv("ESSENTIA_DATA_DIR", saveDir.getAbsolutePath());
            String screen = i.getStringExtra(EXTRA_SCREEN);
            setenv("ESSENTIA_SCREEN_MODE", screen == null || screen.isEmpty() ? SCREEN_ORIGINAL : screen);
            setenv("ESSENTIA_ORIENTATION", "landscape");
            setenv("ESSENTIA_WIDE_BATTLES", SCREEN_WIDE_FULL.equals(screen) ? "1" : "0");
            // The wide picture takes the shape of this screen (a 16:9 console, a 20:9 phone...)
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            float aspect = Math.max(dm.widthPixels, dm.heightPixels) / (float) Math.max(1, Math.min(dm.widthPixels, dm.heightPixels));
            setenv("ESSENTIA_ASPECT", String.format(Locale.US, "%.4f", aspect));
            String filter = i.getStringExtra(EXTRA_FILTER);
            setenv("ESSENTIA_FILTER", filter == null ? "" : filter);
            setenv("ESSENTIA_TILT", String.valueOf(Math.max(0, Math.min(60, i.getIntExtra(EXTRA_TILT, 0)))));
            startTranslator(i);
            setenv("ESSENTIA_LANG", Locale.getDefault().getLanguage());
            File errorFile = new File(getFilesDir(), "last_error.txt");
            //noinspection ResultOfMethodCallIgnored
            errorFile.delete();  // only an error of this session may be reported
            setenv("ESSENTIA_ERROR_FILE", errorFile.getAbsolutePath());
            File scripts = new File(getCacheDir(), "scripts/v" + versionCode() + "/" + digest(game.getAbsolutePath()));
            //noinspection ResultOfMethodCallIgnored
            scripts.mkdirs();
            setenv("ESSENTIA_ISEQ_DIR", scripts.getAbsolutePath());
            // Windows folders some games use (e.g. Dir.chdir(ENV["TEMP"]))
            File tmp = new File(getCacheDir(), "game-tmp");
            //noinspection ResultOfMethodCallIgnored
            tmp.mkdirs();
            for (String k : new String[]{"TEMP", "TMP", "TMPDIR"}) setenv(k, tmp.getAbsolutePath());
            String rtp = i.getStringExtra(EXTRA_RTP_ROOT);
            setenv("ESSENTIA_RTP", rtp == null || rtp.isEmpty() ? "" : findRtp(game, new File(rtp)));
            setenv("SDL_ACCELEROMETER_AS_JOYSTICK", "0");
            if (!mClassic) {
                // Fewer garbage-collection pauses in big Essentials games (modern engine only)
                setenv("RUBY_GC_HEAP_INIT_SLOTS", "600000");
                setenv("RUBY_GC_HEAP_FREE_SLOTS", "150000");
                setenv("RUBY_GC_HEAP_GROWTH_FACTOR", "1.4");
                setenv("RUBY_GC_MALLOC_LIMIT", "50331648");
                setenv("RUBY_GC_MALLOC_LIMIT_MAX", "201326592");
                setenv("RUBY_GC_OLDMALLOC_LIMIT", "50331648");
                setenv("RUBY_GC_OLDMALLOC_LIMIT_MAX", "201326592");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not prepare the game environment", e);
        }
    }

    private static void setenv(String k, String v) throws Exception
    {
        android.system.Os.setenv(k, v, true);
    }

    private int versionCode()
    {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String digest(String s)
    {
        try {
            byte[] h = MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (int k = 0; k < 8; k++) b.append(String.format(Locale.ROOT, "%02x", h[k]));
            return b.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }

    /** RPG Maker RTP folders under the RTP root that match this game's RPG Maker version. */
    static String findRtp(File game, File root)
    {
        String kind = "XP";
        File data = null;
        String[] names = game.list();
        if (names != null) for (String n : names) {
            String l = n.toLowerCase(Locale.ROOT);
            if (l.equals("game.rgss3a")) kind = "VXAce";
            else if (l.equals("game.rgss2a") && !kind.equals("VXAce")) kind = "VX";
            else if (l.equals("data")) data = new File(game, n);
        }
        if (data != null && kind.equals("XP")) {
            String[] d = data.list();
            if (d != null) for (String n : d) {
                String l = n.toLowerCase(Locale.ROOT);
                if (l.endsWith(".rvdata2")) { kind = "VXAce"; break; }
                if (l.endsWith(".rvdata")) kind = "VX";
            }
        }
        String[] wanted = kind.equals("XP") ? new String[]{"XP", "RPGXP", "RMXP", "Standard"}
            : kind.equals("VX") ? new String[]{"VX", "RPGVX", "RMVX"}
            : new String[]{"VXAce", "VX Ace", "RPGVXAce", "RMVXAce", "VXA", "Ace"};
        String[] present = root.list();
        if (present == null) return "";
        StringBuilder out = new StringBuilder();
        for (String w : wanted) for (String p : present) {
            if (p.equalsIgnoreCase(w) && new File(root, p).isDirectory()) {
                if (out.length() > 0) out.append(':');
                out.append(new File(root, p).getAbsolutePath());
            }
        }
        return out.toString();
    }

    /** Android's trusted roots as a PEM file, so HTTPS inside games works with OpenSSL. */
    private static File trustFile(Context c) throws Exception
    {
        // Building the file means reading ~140 certificates; the engine waits for it to start. Android's roots
        // only change with the system or this app, so a file newer than both is reused.
        File cached = new File(c.getCacheDir(), "android-trust.pem");
        try {
            long installed = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).lastUpdateTime;
            if (cached.isFile() && cached.length() > 1000 && cached.lastModified() > installed
                && cached.lastModified() > System.currentTimeMillis() - 7L * 24 * 3600 * 1000) return cached;
        } catch (Exception ignored) { }
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        StringBuilder pem = new StringBuilder();
        for (TrustManager m : factory.getTrustManagers()) if (m instanceof X509TrustManager) {
            for (X509Certificate cert : ((X509TrustManager) m).getAcceptedIssuers()) {
                pem.append("-----BEGIN CERTIFICATE-----\n");
                String enc = Base64.encodeToString(cert.getEncoded(), Base64.NO_WRAP);
                for (int o = 0; o < enc.length(); o += 64) pem.append(enc, o, Math.min(o + 64, enc.length())).append('\n');
                pem.append("-----END CERTIFICATE-----\n");
            }
        }
        if (pem.length() == 0) throw new IOException("No trusted certificates");
        File dest = new File(c.getCacheDir(), "android-trust.pem");
        File temp = new File(c.getCacheDir(), "android-trust-" + java.util.UUID.randomUUID() + ".tmp");
        try {
            try (FileOutputStream out = new FileOutputStream(temp)) {
                out.write(pem.toString().getBytes("US-ASCII"));
                out.getFD().sync();
            }
            if (!temp.renameTo(dest)) throw new IOException("Could not write trusted roots");
            return dest;
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /**
     * This engine's SDLActivity does not start the native thread on its own (on Android 7+ it waits for
     * the activity to ask). Without this call the game never starts and the loading screen stays forever.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        if (mHasMultiWindow) resumeNativeThread();
    }

    @Override
    protected String[] getLibraries()
    {
        if (mClassic) return new String[]{"SDL2", "SDL2_image", "SDL2_ttf", "SDL2_sound", "openal", "mkxp-z-legacy"};
        return super.getLibraries();
    }

    /** Copies the configuration generated by the Android Ruby build to Ruby's expected directory layout. */
    private File rubyConfigDirectory() throws IOException
    {
        String root = "ruby-runtime/lib/ruby/3.1.0";
        String[] architectures = getAssets().list(root);
        if (architectures == null || architectures.length != 1)
            throw new IOException("Missing Android Ruby configuration in the APK");
        String asset = root + "/" + architectures[0] + "/rbconfig.rb";
        File dest = new File(getCacheDir(), "runtime/v" + versionCode() + "/" + asset);
        File directory = dest.getParentFile();
        if (dest.isFile() && dest.length() > 0) return directory;
        if (!directory.mkdirs() && !directory.isDirectory()) throw new IOException("Cannot create Ruby runtime folder");
        File temporary = File.createTempFile("rbconfig-", ".tmp", directory);
        try {
            try (InputStream input = getAssets().open(asset); FileOutputStream output = new FileOutputStream(temporary)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            }
            if (!temporary.renameTo(dest)) throw new IOException("Cannot install Android Ruby configuration");
            return directory;
        } finally {
            temporary.delete();
        }
    }

    @Override
    protected String[] getArguments()
    {
        if (mTrust == null) return new String[0];
        if (!mClassic) {
            try {
                setenv("GAHVORIA_RUBY_ARCHDIR", rubyConfigDirectory().getAbsolutePath());
            } catch (Exception e) {
                Log.w(TAG, "Could not prepare Android Ruby configuration", e);
            }
        }
        try {
            String trust = mTrust.get().getAbsolutePath();
            setenv("ESSENTIA_CA_FILE", trust);
            setenv("SSL_CERT_FILE", trust);
        } catch (Exception e) {
            Log.w(TAG, "Could not prepare trusted roots for the game");
        }
        return new String[]{};
    }

    // ------------------------------------------------------------ input

    /**
     * A small keyboard button (top right) for typing in the game: names, codes, searches. It opens the phone's keyboard
     * through SDL, which sends both the key presses (for games that read keys) and the text (for games that read text).
     */
    private void addKeyboardButton()
    {
        TextView b = new TextView(this);
        b.setText("⌨");
        b.setTextSize(20);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(mLook.ink);
        b.setBackground(mLook.box(Look.withAlpha(mLook.bg2, 0xB0), 14, mLook.line));
        b.setAlpha(0.7f);
        b.setFocusable(false);
        b.setContentDescription("Teclado");
        b.setOnClickListener(v -> toggleKeyboard());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mLook.dp(44), mLook.dp(44));
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.topMargin = mLook.dp(12);
        lp.rightMargin = mLook.dp(12);
        mLayout.addView(b, lp);
        mKeyboardButton = b;
    }

    private TypingView mTyping;
    private boolean mTypingOpen;

    /** Gahvoria native (eventthread.cpp): puts typed text into mkxp-z's text buffer, read by Input.gets. */
    private static native void enqueueKeyboardText(byte[] utf8);

    private void toggleKeyboard()
    {
        android.view.inputmethod.InputMethodManager imm =
            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        try {
            if (mTyping == null) {
                mTyping = new TypingView(this, text -> {
                    try { enqueueKeyboardText(TypingView.utf8(text)); } catch (Throwable t) { Log.w(TAG, "Typed text not delivered", t); }
                });
                mTyping.onClosed = this::closeKeyboard;
                mLayout.addView(mTyping, new RelativeLayout.LayoutParams(1, 1));
            }
            if (mTypingOpen && imeVisible()) { closeKeyboard(); return; }
            mTyping.requestFocus();
            imm.showSoftInput(mTyping, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            mTypingOpen = true;
        } catch (Throwable t) {
            Log.w(TAG, "Keyboard button failed", t);
            notice("No pude abrir el teclado.", mLook.warn);
        }
    }

    /** Whether the phone's keyboard is on screen (Android 11+ knows it; before, the button's own memory is used). */
    private boolean imeVisible()
    {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            android.view.WindowInsets in = getWindow().getDecorView().getRootWindowInsets();
            if (in != null) return in.isVisible(android.view.WindowInsets.Type.ime());
        }
        return mTypingOpen;
    }

    private void closeKeyboard()
    {
        mTypingOpen = false;
        try {
            android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (mTyping != null) imm.hideSoftInputFromWindow(mTyping.getWindowToken(), 0);
            if (mSurface != null) mSurface.requestFocus();   // the controls go back to the game
        } catch (Throwable t) {
            Log.w(TAG, "Closing the keyboard failed", t);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent evt)
    {
        int code = evt.getKeyCode();
        if (sScreen != null && sScreen.handleKey(evt)) return true;
        if (code == KeyEvent.KEYCODE_BACK) {
            if (evt.getAction() == KeyEvent.ACTION_UP) askExit();
            return true;
        }
        boolean volume = code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN
            || code == KeyEvent.KEYCODE_VOLUME_MUTE || code == KeyEvent.KEYCODE_HEADSETHOOK;
        if (!volume && !mPadHidden) {
            // A physical controller or keyboard is in use: hide the touch controls.
            mGamepad.hideView();
            mPadHidden = true;
            if (mKeyboardButton != null) mKeyboardButton.animate().alpha(0f).setDuration(500).start();
        }
        if (mGamepad.processGamepadEvent(evt)) return true;
        return super.dispatchKeyEvent(evt);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent evt)
    {
        if (mPadHidden && mTouch) {
            mGamepad.showView();
            mPadHidden = false;
            if (mKeyboardButton != null) mKeyboardButton.animate().alpha(0.7f).setDuration(250).start();
        }
        return super.dispatchTouchEvent(evt);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent evt)
    {
        mGamepad.processDPadEvent(evt);
        mGamepad.processStickEvent(evt);
        return super.dispatchGenericMotionEvent(evt);
    }

    private void askExit()
    {
        if (mExit != null && mExit.isShowing()) return;
        mExit = new GameDialog(this, mLook, "¿Salir del juego?", "Lo que no hayas guardado dentro del juego se pierde.",
            "Seguir jugando", "Salir", this::finish);
        mExit.show();
    }

    // ------------------------------------------------------------ notices, report and load times

    /** A short notice in the app's style (palette colors), over the game. */
    private void notice(String text, int color)
    {
        if (mLayout == null || sMain == null) return;
        sMain.post(() -> {
            TextView t = new TextView(this);
            t.setText(text);
            t.setTextColor(mLook.ink);
            t.setTextSize(13);
            t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            t.setPadding(mLook.dp(14), mLook.dp(11), mLook.dp(18), mLook.dp(11));
            t.setBackground(mLook.box(Look.withAlpha(mLook.bg2, 0xF0), 18, mLook.line));
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(color);
            dot.setSize(mLook.dp(10), mLook.dp(10));
            t.setCompoundDrawablesWithIntrinsicBounds(dot, null, null, null);
            t.setCompoundDrawablePadding(mLook.dp(10));
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.bottomMargin = mLook.dp(28);
            t.setAlpha(0f);
            mLayout.addView(t, lp);
            t.animate().alpha(1f).setDuration(200).start();
            sMain.postDelayed(() -> t.animate().alpha(0f).setDuration(300).withEndAction(() -> mLayout.removeView(t)).start(), 3500);
        });
    }

    private void copyText(String text)
    {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Informe de Gahvoria", text));
    }

    private String buildReport(long[] t, long totalMs)
    {
        StringBuilder b = new StringBuilder();
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            b.append("Gahvoria ").append(info.versionName).append(" (código ").append(info.versionCode).append(")\n");
        } catch (Exception ignored) { }
        b.append("Juego: ").append(mTitle).append('\n');
        b.append("Motor: ").append(mClassic ? "clásico" : "moderno").append('\n');
        b.append("Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append("), ")
            .append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
        b.append("Pasos: leyendo archivos ").append(LoadingScreen.sec(t[0])).append(", preparando scripts ")
            .append(LoadingScreen.sec(t[1])).append(", abriendo ").append(LoadingScreen.sec(t[2])).append('\n');
        b.append("Tiempo: ").append(LoadingScreen.sec(totalMs)).append('\n');
        File err = new File(getFilesDir(), "last_error.txt");
        if (err.isFile()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(err)) {
                byte[] buf = new byte[3000];
                int n = in.read(buf);
                if (n > 0) b.append("\nError del juego:\n").append(new String(buf, 0, n, "UTF-8"));
            } catch (Exception ignored) { }
        }
        return b.toString();
    }

    /** One line per load, kept on the device only: when, version, game, engine and the three steps. Last 200 are kept. */
    private void saveLoadTimes(long[] t, long totalMs)
    {
        new Thread(() -> {
            try {
                File f = new File(getFilesDir(), "load_times.tsv");
                java.util.List<String> keep = new java.util.ArrayList<>();
                if (f.isFile()) {
                    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), "UTF-8"))) {
                        String line;
                        while ((line = r.readLine()) != null) keep.add(line);
                    }
                }
                keep.add(System.currentTimeMillis() + "\t" + versionCode() + "\t" + mTitle.replace('\t', ' ').replace('\n', ' ')
                    + "\t" + (mClassic ? ENGINE_CLASSIC : ENGINE_MODERN) + "\t" + t[0] + "\t" + t[1] + "\t" + t[2] + "\t" + totalMs);
                while (keep.size() > 200) keep.remove(0);
                try (FileWriter w = new FileWriter(f, false)) {
                    for (String line : keep) w.write(line + "\n");
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not save load times", e);
            }
        }, "GahvoriaLoadTimes").start();
    }

    @Override
    protected void onDestroy()
    {
        if (mTrust != null) mTrust.cancel(true);
        String catalog = getIntent().getStringExtra(EXTRA_TRANSLATION_CATALOG);
        if (catalog != null && !new File(catalog + ".ready").isFile() && !new File(catalog + ".error").isFile()) {
            try { new File(catalog + ".skip").createNewFile(); }
            catch (IOException failure) { Log.w(TAG, "Could not close translation scan", failure); }
        }
        if (mTranslator != null) mTranslator.stop();
        if (mPreparesTranslation && isFinishing()) {
            translationPreparationMarker(this, getIntent().getLongExtra(EXTRA_LAUNCH_AT, 0L)).delete();
        }
        super.onDestroy();
        // Ruby cannot start again in the same process: end it together with the game.
        System.exit(0);
    }

    // ------------------------------------------------------------ called by the native engine

    @SuppressWarnings("unused")
    private static void onLoadPhase(String phase)
    {
        if (sMain == null) return;
        sMain.post(() -> { if (sScreen != null) sScreen.setPhase(phase); });
    }

    @SuppressWarnings("unused")
    private static void onGameReady()
    {
        if (sMain == null) return;
        sMain.post(() -> {
            if (sScreen != null) sScreen.finishLoading();
            sScreen = null;
        });
    }

    @SuppressWarnings("unused")
    private static void updateFPSText(int num)
    {
        if (sMain != null) sMain.post(() -> { if (sFps != null) sFps.setText(num + " FPS"); });
    }

    @SuppressWarnings("unused")
    private static void setFPSVisibility(boolean visible)
    {
        if (sMain != null) sMain.post(() -> { if (sFps != null) sFps.setVisibility(visible ? View.VISIBLE : View.GONE); });
    }

    @SuppressWarnings("unused")
    private static String getSystemLanguage()
    {
        return Locale.getDefault().toString();
    }

    @SuppressWarnings("unused")
    private static boolean hasVibrator()
    {
        return sVibrator != null && sVibrator.hasVibrator();
    }

    @SuppressWarnings("unused")
    private static void vibrate(int duration)
    {
        if (sVibrator == null) return;
        duration = Math.min(duration, 10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sVibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            sVibrator.vibrate(duration);
    }

    @SuppressWarnings("unused")
    private static void vibrateStop()
    {
        if (sVibrator != null) sVibrator.cancel();
    }

    @SuppressWarnings("unused")
    private static boolean inMultiWindow(Activity activity)
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }
}
