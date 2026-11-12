package com.hatkid.mkxpz;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Gahvoria's own question box for the game screen: glass over the palette, controller friendly (A chooses, B goes back). */
final class GameDialog extends Dialog
{
    private final Look look;

    /** Asks {@code title}; "keep" closes it, "leave" runs {@code onLeave}. */
    GameDialog(Context c, Look look, String title, String message, String keep, String leave, Runnable onLeave)
    {
        super(c);
        this.look = look;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(look.dp(20), look.dp(20), look.dp(20), look.dp(20));
        box.setBackground(look.box(Look.withAlpha(look.bg2, 0xF2), 24, look.line));

        TextView t = new TextView(c);
        t.setText(title);
        t.setTextColor(look.ink);
        t.setTextSize(17);
        t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        box.addView(t);

        TextView m = new TextView(c);
        m.setText(message);
        m.setTextColor(look.muted);
        m.setTextSize(13);
        m.setPadding(0, look.dp(8), 0, look.dp(16));
        box.addView(m);

        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView keepBtn = button(c, keep, true);
        TextView leaveBtn = button(c, leave, false);
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, -2, 1f);
        a.rightMargin = look.dp(4);
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, -2, 1f);
        b.leftMargin = look.dp(4);
        row.addView(keepBtn, a);
        row.addView(leaveBtn, b);
        box.addView(row);

        keepBtn.setOnClickListener(v -> dismiss());
        leaveBtn.setOnClickListener(v -> { dismiss(); onLeave.run(); });
        setContentView(box);
        setOnShowListener(d -> keepBtn.requestFocus());
    }

    private TextView button(Context c, String text, boolean primary)
    {
        TextView v = new TextView(c);
        v.setText(text);
        v.setGravity(Gravity.CENTER);
        v.setTextSize(13);
        v.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        v.setTextColor(primary ? look.onAccent : look.ink);
        v.setPadding(look.dp(8), look.dp(13), look.dp(8), look.dp(13));
        v.setFocusable(true);
        v.setClickable(true);
        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[]{android.R.attr.state_focused}, look.box(primary ? look.accent : Look.withAlpha(look.ink, 0x26), 14, look.accent3));
        bg.addState(new int[]{}, primary ? look.gradient(14) : look.box(Look.withAlpha(look.ink, 0x14), 14, look.line));
        v.setBackground(bg);
        return v;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Window w = getWindow();
        if (w == null) return;
        w.setBackgroundDrawable(new ColorDrawable(0));
        w.setDimAmount(0.6f);
        w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        w.setLayout(Math.min(look.dp(420), (int) (dm.widthPixels * 0.9f)), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    /** On a controller, A presses what has focus and B goes back. */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
            if (e.getAction() == KeyEvent.ACTION_UP) dismiss();
            return true;
        }
        if (e.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
            return super.dispatchKeyEvent(new KeyEvent(e.getAction(), KeyEvent.KEYCODE_DPAD_CENTER));
        }
        return super.dispatchKeyEvent(e);
    }
}
