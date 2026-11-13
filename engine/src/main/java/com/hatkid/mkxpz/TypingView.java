package com.hatkid.mkxpz;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.libsdl.app.SDLActivity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

/**
 * Gahvoria: the phone's keyboard for typing in games (names, codes).
 *
 * Old RPG Maker games don't receive text: every frame they look at which keys are held. SDL's own keyboard presses and
 * releases a key in the same instant, so those games never see it. Here each letter, Backspace and Enter becomes a real
 * key press that is held for a few frames, one after another. The text is also sent as text (Input.gets) for the games
 * that read it that way.
 */
final class TypingView extends View
{
    interface Sink
    {
        /** Text for the games that read typed text (mkxp-z Input.gets). */
        void text(String s);
    }

    private static final long HOLD_MS = 80;   // about five frames at 60 fps
    private static final long GAP_MS = 45;

    private final Handler mMain = new Handler(Looper.getMainLooper());
    private final ArrayDeque<int[]> mQueue = new ArrayDeque<>();  // {keycode, shift(0/1)}
    private final KeyCharacterMap mMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    private final Sink mSink;
    private boolean mBusy;
    Runnable onClosed;

    TypingView(Context c, Sink sink)
    {
        super(c);
        mSink = sink;
        setFocusable(true);
        setFocusableInTouchMode(true);
        // A controller used while the keyboard is open still reaches the game.
        setOnKeyListener((v, code, e) -> {
            if (code == KeyEvent.KEYCODE_BACK) return false;
            return SDLActivity.handleKeyEvent(v, code, e, null);
        });
    }

    @Override
    public boolean onCheckIsTextEditor()
    {
        return true;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        // Back while the keyboard is open closes it: give the controls back to the game.
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && onClosed != null)
            mMain.post(onClosed);
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo out)
    {
        // TYPE_NULL asks the keyboard to send plain key presses (letters, Backspace, Enter) instead of editing a text box.
        out.inputType = InputType.TYPE_NULL;
        out.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_ACTION_DONE;
        return new BaseInputConnection(this, false)
        {
            @Override
            public boolean sendKeyEvent(KeyEvent e)
            {
                if (e.getAction() != KeyEvent.ACTION_DOWN) return true;
                int code = e.getKeyCode();
                int ch = e.getUnicodeChar();
                if (code == KeyEvent.KEYCODE_DEL || code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_SPACE || ch == 0) {
                    press(code, e.isShiftPressed());
                    if (code == KeyEvent.KEYCODE_SPACE) mSink.text(" ");
                } else {
                    typeText(String.valueOf((char) ch));
                }
                return true;
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition)
            {
                typeText(text.toString());
                return true;
            }

            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition)
            {
                // Some keyboards still compose; only the finished text counts (commitText / finishComposingText).
                return true;
            }

            @Override
            public boolean deleteSurroundingText(int before, int after)
            {
                for (int i = 0; i < Math.max(1, before); i++) press(KeyEvent.KEYCODE_DEL, false);
                return true;
            }

            @Override
            public boolean performEditorAction(int action)
            {
                press(KeyEvent.KEYCODE_ENTER, false);
                return true;
            }
        };
    }

    /** One press per character it can be typed with (letters, digits, symbols), plus the text itself. */
    void typeText(String s)
    {
        if (s.isEmpty()) return;
        mSink.text(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') { press(KeyEvent.KEYCODE_ENTER, false); continue; }
            KeyEvent[] evs = mMap.getEvents(new char[]{Character.toLowerCase(c) == c ? c : Character.toLowerCase(c)});
            boolean shift = Character.isUpperCase(c);
            if (evs == null) continue;   // letters with accents: only as text
            for (KeyEvent e : evs) {
                if (e.getAction() != KeyEvent.ACTION_DOWN || KeyEvent.isModifierKey(e.getKeyCode())) continue;
                shift = shift || e.isShiftPressed();
                press(e.getKeyCode(), shift);
                break;
            }
        }
    }

    void press(int keycode, boolean shift)
    {
        mQueue.add(new int[]{keycode, shift ? 1 : 0});
        if (!mBusy) next();
    }

    private void next()
    {
        int[] k = mQueue.poll();
        if (k == null) { mBusy = false; return; }
        mBusy = true;
        final int code = k[0];
        final boolean shift = k[1] == 1;
        if (shift) SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT);
        SDLActivity.onNativeKeyDown(code);
        mMain.postDelayed(() -> {
            SDLActivity.onNativeKeyUp(code);
            if (shift) SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT);
            mMain.postDelayed(this::next, GAP_MS);
        }, HOLD_MS);
    }

    static byte[] utf8(String s)
    {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
