package com.hatkid.mkxpz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * What the player sees until the game draws its first frame: which step it is on, how long each one took and, when
 * a step makes no progress, whether it is just slow or looks stuck. It never closes the game by itself: a big game
 * can take long on purpose, so the player decides (keep waiting, leave, copy a report).
 */
final class LoadingScreen extends FrameLayout
{
    interface Host
    {
        void exit();

        /** Copies a report for the developer; {@code times} are the step durations in ms. */
        void copyReport(long[] times, long totalMs);

        /** The game drew its first frame. */
        void loaded(long[] times, long totalMs);
    }

    private enum State { LOAD, SLOW, STUCK }

    private static final String[] PHASES = {"Leyendo archivos", "Preparando scripts", "Abriendo el juego"};
    private static final String[] DOING = {"Leyendo los archivos del juego…", "Preparando los scripts del juego…", "Abriendo el juego…"};
    /** No change of step for this long: "Tarda más de lo normal". Tuned later with real games on the Thor. */
    static final long SLOW_MS = 20_000;
    /** ...and for this long: "Parece pegado". */
    static final long STUCK_MS = 60_000;

    private final Look look;
    private final Host host;
    private final long started;
    private final long[] times = new long[3];
    private int phase = 0;
    private long phaseStarted;
    private long lastChange;
    private long waitFrom;
    private boolean finished;
    private State shown;

    private final TextView clock;
    private final TextView badge;
    private final TextView message;
    private final TextView trace;
    private final TextView[] phaseTimes = new TextView[3];
    private final TextView[] phaseNames = new TextView[3];
    private final View[] dots = new View[3];
    private final LinearLayout actions;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable()
    {
        @Override
        public void run()
        {
            if (finished) return;
            refresh();
            ui.postDelayed(this, 100);
        }
    };

    LoadingScreen(Context c, Look look, String title, String info, long startedAt, Host host)
    {
        super(c);
        this.look = look;
        this.host = host;
        this.started = startedAt;
        this.phaseStarted = startedAt;
        this.lastChange = startedAt;
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{look.bg, look.bg2}));
        setClickable(false);

        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(look.dp(40), look.dp(24), look.dp(40), look.dp(24));
        addView(root, new LayoutParams(-1, -1));

        // left: who is loading
        LinearLayout left = new LinearLayout(c);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(left, new LinearLayout.LayoutParams(0, -2, 1f));
        left.addView(new Cartridge(c, look), new LinearLayout.LayoutParams(look.dp(64), look.dp(64)));
        TextView name = text(c, title == null ? "" : title, 24, look.ink, true);
        name.setPadding(0, look.dp(14), look.dp(16), 0);
        left.addView(name);
        TextView engine = text(c, info == null ? "" : info, 12, look.muted, false);
        engine.setPadding(0, look.dp(4), 0, look.dp(14));
        left.addView(engine);
        clock = text(c, "00:00.0", 38, look.ink, true);
        clock.setFontFeatureSettings("tnum");
        left.addView(clock);

        // right: where it is
        LinearLayout right = new LinearLayout(c);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(right, new LinearLayout.LayoutParams(0, -2, 1.15f));
        badge = text(c, "", 10, look.accent3, true);
        badge.setAllCaps(true);
        badge.setLetterSpacing(0.1f);
        badge.setPadding(look.dp(12), look.dp(5), look.dp(12), look.dp(5));
        right.addView(badge, new LinearLayout.LayoutParams(-2, -2));

        LinearLayout list = new LinearLayout(c);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, look.dp(14), 0, look.dp(6));
        for (int i = 0; i < 3; i++) {
            LinearLayout row = new LinearLayout(c);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, look.dp(4), 0, look.dp(4));
            dots[i] = new View(c);
            row.addView(dots[i], new LinearLayout.LayoutParams(look.dp(12), look.dp(12)));
            phaseNames[i] = text(c, PHASES[i], 13, look.faint, false);
            phaseNames[i].setPadding(look.dp(10), 0, 0, 0);
            row.addView(phaseNames[i], new LinearLayout.LayoutParams(0, -2, 1f));
            phaseTimes[i] = text(c, "", 13, look.muted, false);
            phaseTimes[i].setFontFeatureSettings("tnum");
            row.addView(phaseTimes[i]);
            list.addView(row);
        }
        right.addView(list);

        message = text(c, "", 13, look.muted, false);
        right.addView(message);
        trace = text(c, "", 11, look.muted, false);
        trace.setTypeface(Typeface.MONOSPACE);
        trace.setVisibility(GONE);
        right.addView(trace);
        actions = new LinearLayout(c);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, look.dp(14), 0, 0);
        right.addView(actions);

        refresh();
        ui.postDelayed(tick, 100);
    }

    private TextView text(Context c, String s, float sp, int color, boolean bold)
    {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    // ------------------------------------------------------------ from the engine (UI thread)

    /** The engine entered a step: "files", "scripts" or "start". Steps only move forward. */
    void setPhase(String name)
    {
        int i = "files".equals(name) ? 0 : "scripts".equals(name) ? 1 : "start".equals(name) ? 2 : -1;
        if (finished || i <= phase) return;
        long now = SystemClock.elapsedRealtime();
        times[phase] = now - phaseStarted;
        for (int k = phase + 1; k < i; k++) times[k] = 0;
        phase = i;
        phaseStarted = now;
        lastChange = now;
        waitFrom = 0;
        refresh();
    }

    /** The first frame was drawn: record the times and go away. */
    void finishLoading()
    {
        if (finished) return;
        finished = true;
        ui.removeCallbacks(tick);
        long now = SystemClock.elapsedRealtime();
        times[phase] = now - phaseStarted;
        host.loaded(times.clone(), now - started);
        if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
    }

    // ------------------------------------------------------------ what is shown

    private void refresh()
    {
        long now = SystemClock.elapsedRealtime();
        times[phase] = now - phaseStarted;
        clock.setText(clock(now - started));
        for (int i = 0; i < 3; i++) {
            boolean done = i < phase, on = i == phase;
            phaseNames[i].setTextColor(on ? look.ink : done ? look.muted : look.faint);
            phaseTimes[i].setText(i <= phase ? sec(times[i]) : "");
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            if (done) d.setColor(look.ok);
            else if (on) d.setColor(look.accent);
            else d.setStroke(look.dp(2), look.faint);
            dots[i].setBackground(d);
            dots[i].setAlpha(on ? (float) (0.65 + 0.35 * Math.sin(now / 250.0)) : 1f);
        }
        long stall = now - Math.max(lastChange, waitFrom);
        State s = stall >= STUCK_MS ? State.STUCK : stall >= SLOW_MS ? State.SLOW : State.LOAD;
        String where = "«" + PHASES[phase] + "»";
        if (s == State.LOAD) {
            badge(look.accent3, "Cargando");
            message.setText(DOING[phase]);
        } else if (s == State.SLOW) {
            badge(look.warn, "Tarda más de lo normal");
            message.setText(bold("Lleva ", stallText(stall), " en " + where + ". Sigue trabajando."));
        } else {
            badge(look.accent, "Parece pegado");
            message.setText(bold("No avanza hace ", stallText(stall), " en " + where + ". Puedes seguir esperando o salir."));
        }
        if (s != shown) {
            shown = s;
            buildActions(s);
        }
    }

    private void badge(int color, String text)
    {
        badge.setText(text);
        badge.setTextColor(color);
        badge.setBackground(look.box(Look.withAlpha(color, 0x2E), 999, 0));
    }

    private void buildActions(State s)
    {
        actions.removeAllViews();
        if (s == State.LOAD) return;
        if (s == State.STUCK) addAction("Seguir esperando", true, () -> { waitFrom = SystemClock.elapsedRealtime(); refresh(); });
        addAction("Salir", s == State.SLOW, host::exit);
        if (s == State.STUCK) addAction("Copiar informe", false, () -> host.copyReport(times.clone(), SystemClock.elapsedRealtime() - started));
        if (actions.getChildCount() > 0) actions.getChildAt(0).requestFocus();
    }

    private void addAction(String label, boolean primary, Runnable run)
    {
        TextView v = text(getContext(), label, 12, primary ? look.onAccent : look.ink, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(look.dp(14), look.dp(11), look.dp(14), look.dp(11));
        v.setFocusable(true);
        v.setClickable(true);
        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[]{android.R.attr.state_focused}, look.box(primary ? look.accent : Look.withAlpha(look.ink, 0x26), 14, look.accent3));
        bg.addState(new int[]{}, primary ? look.gradient(14) : look.box(Look.withAlpha(look.ink, 0x14), 14, look.line));
        v.setBackground(bg);
        v.setOnClickListener(x -> run.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = look.dp(8);
        actions.addView(v, lp);
    }

    /** Controller while the buttons are visible: left/right move, A presses. Returns true when it used the key. */
    boolean handleKey(KeyEvent e)
    {
        if (finished || actions.getChildCount() == 0) return false;
        int k = e.getKeyCode();
        boolean confirm = k == KeyEvent.KEYCODE_BUTTON_A || k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER;
        boolean move = k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT;
        if (!confirm && !move) return false;
        if (e.getAction() != KeyEvent.ACTION_UP) return true;
        View f = actions.findFocus();
        if (confirm) {
            if (f != null) f.performClick();
            else actions.getChildAt(0).requestFocus();
        } else {
            View next = f == null ? actions.getChildAt(0) : f.focusSearch(k == KeyEvent.KEYCODE_DPAD_LEFT ? View.FOCUS_LEFT : View.FOCUS_RIGHT);
            if (next != null && next.getParent() == actions) next.requestFocus();
        }
        return true;
    }

    // ------------------------------------------------------------ text helpers

    private static String clock(long ms)
    {
        long tenths = ms / 100;
        return String.format(Locale.ROOT, "%02d:%02d.%d", tenths / 600, (tenths / 10) % 60, tenths % 10);
    }

    static String sec(long ms)
    {
        return String.format(Locale.ROOT, "%.1f s", ms / 1000.0).replace('.', ',');
    }

    private static String stallText(long ms)
    {
        long s = ms / 1000;
        if (s < 60) return sec(ms);
        return (s / 60) + " min " + String.format(Locale.ROOT, "%02d", s % 60) + " s";
    }

    private static CharSequence bold(String before, String strong, String after)
    {
        SpannableStringBuilder b = new SpannableStringBuilder(before);
        int start = b.length();
        b.append(strong);
        b.setSpan(new StyleSpan(Typeface.BOLD), start, b.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        b.append(after);
        return b;
    }

    // ------------------------------------------------------------ the cartridge, as in the app's logo

    private static final class Cartridge extends View
    {
        private final Look look;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        Cartridge(Context c, Look look)
        {
            super(c);
            this.look = look;
        }

        private void poly(Canvas c, float k, float... p)
        {
            path.reset();
            path.moveTo(p[0] * k, p[1] * k);
            for (int i = 2; i < p.length; i += 2) path.lineTo(p[i] * k, p[i + 1] * k);
            path.close();
            c.drawPath(path, paint);
        }

        @Override
        protected void onDraw(Canvas c)
        {
            float k = Math.min(getWidth(), getHeight()) / 100f;
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, 0, 100 * k, new int[]{look.accent, look.accent2, look.accent3}, null, Shader.TileMode.CLAMP));
            poly(c, k, 30, 6, 70, 6, 84, 20, 84, 90, 78, 96, 22, 96, 16, 90, 16, 20);
            paint.setShader(null);
            paint.setColor(look.bg);
            c.drawRoundRect(new RectF(25 * k, 20 * k, 75 * k, 58 * k), 5 * k, 5 * k, paint);
            paint.setColor(look.accent);
            poly(c, k, 50, 25, 62, 39, 50, 53, 38, 39);
            paint.setColor(0x8CFFFFFF);
            poly(c, k, 50, 25, 62, 39, 50, 41);
            paint.setColor(look.bg);
            for (float x : new float[]{31, 42, 53, 64}) c.drawRoundRect(new RectF(x * k, 74 * k, (x + 4) * k, 90 * k), 2 * k, 2 * k, paint);
        }
    }
}
