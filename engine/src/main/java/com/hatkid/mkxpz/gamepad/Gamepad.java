package com.hatkid.mkxpz.gamepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.hatkid.mkxpz.R;
import com.hatkid.mkxpz.utils.ViewUtils;

public class Gamepad
{
    private GamepadConfig mGamepadConfig = null;
    private boolean mInvisible = false;

    private OnKeyDownListener mOnKeyDownListener = key -> {};
    private OnKeyUpListener mOnKeyUpListener = key -> {};

    public interface OnKeyDownListener
    {
        void onKeyDown(int key);
    }

    public interface OnKeyUpListener
    {
        void onKeyUp(int key);
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener)
    {
        mOnKeyDownListener = onKeyDownListener;
    }

    /** Essentia actions on controller buttons (turbo, Auto Fire, screenshot...). */
    public interface OnActionListener
    {
        void onAction(String action, boolean down);
    }

    private OnActionListener mOnActionListener = (a, d) -> {};

    /** Short vibration when touching the on-screen controls. */
    public static boolean sHaptics = true;

    public void setOnActionListener(OnActionListener l)
    {
        mOnActionListener = l;
    }

    public void setOnKeyUpListener(OnKeyUpListener onKeyUpListener)
    {
        mOnKeyUpListener = onKeyUpListener;
    }

    private RelativeLayout mGamepadLayout;
    private String mLayoutSpec = "";

    /** The player's arrangement ("piece,x,y,scale,hidden;..."): each piece moves by a fraction of the screen. */
    public void setLayout(String spec)
    {
        mLayoutSpec = spec == null ? "" : spec;
    }

    private void applyLayout()
    {
        if (mLayoutSpec.isEmpty() || mGamepadLayout == null) return;
        mGamepadLayout.post(() -> {
            float w = mGamepadLayout.getWidth();
            float h = mGamepadLayout.getHeight();
            if (w <= 0 || h <= 0) return;
            for (String part : mLayoutSpec.split(";")) {
                String[] f = part.split(",");
                if (f.length < 5) continue;
                int id;
                switch (f[0]) {
                    case "dpad": id = R.id.dpad; break;
                    case "l": id = R.id.buttons_misc_left_layout; break;
                    case "r": id = R.id.buttons_misc_right_layout; break;
                    case "act": id = R.id.buttons_action_layout; break;
                    case "bar": id = R.id.buttons_mod_layout; break;
                    default: continue;
                }
                android.view.View v = mGamepadLayout.findViewById(id);
                if (v == null) continue;
                try {
                    float x = Float.parseFloat(f[1]), y = Float.parseFloat(f[2]), sc = Float.parseFloat(f[3]);
                    if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(sc)) continue;
                    v.setTranslationX(x * w);
                    v.setTranslationY(y * h);
                    v.setPivotX(v.getWidth() / 2f);
                    v.setPivotY(v.getHeight() / 2f);
                    v.setScaleX(sc);
                    v.setScaleY(sc);
                    v.setVisibility("1".equals(f[4]) ? android.view.View.GONE : android.view.View.VISIBLE);
                } catch (NumberFormatException ignored) { }
            }
        });
    }

    // Gamepad buttons
    private GamepadButton gpadBtnA;
    private GamepadButton gpadBtnB;
    private GamepadButton gpadBtnC;
    private GamepadButton gpadBtnX;
    private GamepadButton gpadBtnY;
    private GamepadButton gpadBtnZ;
    private GamepadButton gpadBtnL;
    private GamepadButton gpadBtnR;
    private GamepadButton gpadBtnCTRL;
    private GamepadButton gpadBtnALT;
    private GamepadButton gpadBtnSHIFT;

    public void init(GamepadConfig gpadConfig, boolean invisible)
    {
        mGamepadConfig = gpadConfig;
        mInvisible = invisible;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void attachTo(Context context, ViewGroup viewGroup)
    {
        // Setup layout of in-screen gamepad
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.gamepad_layout, viewGroup);
        mGamepadLayout = layout.findViewById(R.id.gamepad_layout);

        if (mInvisible) {
            mGamepadLayout.setAlpha(0);
        }

        // Setup D-Pad and buttons
        GamepadDPad gpadDPad = layout.findViewById(R.id.dpad);
        gpadBtnA = layout.findViewById(R.id.button_A);
        gpadBtnB = layout.findViewById(R.id.button_B);
        gpadBtnC = layout.findViewById(R.id.button_C);
        gpadBtnX = layout.findViewById(R.id.button_X);
        gpadBtnY = layout.findViewById(R.id.button_Y);
        gpadBtnZ = layout.findViewById(R.id.button_Z);
        gpadBtnL = layout.findViewById(R.id.button_L);
        gpadBtnR = layout.findViewById(R.id.button_R);
        gpadBtnCTRL = layout.findViewById(R.id.button_CTRL);
        gpadBtnALT = layout.findViewById(R.id.button_ALT);
        gpadBtnSHIFT = layout.findViewById(R.id.button_SHIFT);

        // Setup in-screen gamepad listeners
        mGamepadLayout.setOnTouchListener((view, motionEvent) -> false);
        gpadDPad.setOnKeyDownListener(key -> mOnKeyDownListener.onKeyDown(key));
        gpadDPad.setOnKeyUpListener(key -> mOnKeyUpListener.onKeyUp(key));

        // Configure gamepad
        gpadDPad.isDiagonal = mGamepadConfig.diagonalMovement;

        // Setup buttons for gamepad
        initGamepadButtons();

        // Apply scale and opacity from gamepad config
        ViewUtils.resize(mGamepadLayout, mGamepadConfig.scale);
        ViewUtils.changeOpacity(mGamepadLayout, mGamepadConfig.opacity);
        applyLayout();
    }

    private void setGamepadButtonKey(GamepadButton gpadBtn, Integer keycode, String label)
    {
        if (gpadBtn == null) return;

        // Set gamepad button
        gpadBtn.setForegroundText(label);
        gpadBtn.setKey(keycode);
        gpadBtn.setOnKeyDownListener(key -> mOnKeyDownListener.onKeyDown(key));
        gpadBtn.setOnKeyUpListener(key -> mOnKeyUpListener.onKeyUp(key));
    }

    /** Essentia: remove the on-screen controls completely ("never" mode). */
    public void detach()
    {
        if (mGamepadLayout != null) {
            mGamepadLayout.clearAnimation();
            mGamepadLayout.setVisibility(android.view.View.GONE);
        }
    }

    public void showView()
    {
        if (mGamepadLayout != null) {
            if (mGamepadLayout.getAlpha() == 0)
                mGamepadLayout.setAlpha(1);

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setFillAfter(true);
            mGamepadLayout.startAnimation(anim);
        }
    }

    public void hideView()
    {
        if (mGamepadLayout != null) {
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(500);
            anim.setFillAfter(true);
            mGamepadLayout.startAnimation(anim);
        }
    }

    private void initGamepadButtons()
    {
        GamepadConfig c = mGamepadConfig;
        setGamepadButtonKey(gpadBtnA, c.keycodeA, c.labelA);
        setGamepadButtonKey(gpadBtnB, c.keycodeB, c.labelB);
        setGamepadButtonKey(gpadBtnC, c.keycodeC, c.labelC);
        setGamepadButtonKey(gpadBtnX, c.keycodeX, c.labelX);
        setGamepadButtonKey(gpadBtnY, c.keycodeY, c.labelY);
        setGamepadButtonKey(gpadBtnZ, c.keycodeZ, c.labelZ);
        setGamepadButtonKey(gpadBtnL, c.keycodeL, c.labelL);
        setGamepadButtonKey(gpadBtnR, c.keycodeR, c.labelR);
        setGamepadButtonKey(gpadBtnCTRL, c.keycodeCTRL, c.labelCTRL);
        setGamepadButtonKey(gpadBtnALT, c.keycodeALT, c.labelALT);
        setGamepadButtonKey(gpadBtnSHIFT, c.keycodeSHIFT, c.labelSHIFT);
    }

    /**
     * Essentia: physical controller buttons -> keyboard keys that RPG Maker games
     * understand. The engine only reads these keycodes as keyboard keys, so raw
     * gamepad keycodes (BUTTON_A...) would otherwise do nothing in game.
     * The user can change them (ButtonMap); the defaults are:
     *
     *   A -> Enter (confirm)          B -> Esc (cancel / menu)
     *   X -> Shift (RGSS "A")         Y -> A key (RGSS "X")
     *   L1 -> Q (L)   R1 -> W (R)     L2 -> S (Y)   R2 -> D (Z)
     *   Start -> Esc (menu)           L3 -> S      R3 -> D
     *   Select: modifier (Select + Start = exit menu, handled by MainActivity)
     *
     * Returns the keyboard keycode to send, or the same keycode if it isn't a
     * gamepad button, or -1 if the button must be swallowed.
     */
    public static int mapControllerKey(int keycode)
    {
        // Gahvoria: fixed defaults for now; per-game remapping arrives with the new controls screen.
        switch (keycode) {
            case KeyEvent.KEYCODE_BUTTON_A:      return KeyEvent.KEYCODE_ENTER;
            case KeyEvent.KEYCODE_BUTTON_B:      return KeyEvent.KEYCODE_ESCAPE;
            case KeyEvent.KEYCODE_BUTTON_X:      return KeyEvent.KEYCODE_SHIFT_LEFT;
            case KeyEvent.KEYCODE_BUTTON_Y:      return KeyEvent.KEYCODE_A;
            case KeyEvent.KEYCODE_BUTTON_L1:     return KeyEvent.KEYCODE_Q;
            case KeyEvent.KEYCODE_BUTTON_R1:     return KeyEvent.KEYCODE_W;
            case KeyEvent.KEYCODE_BUTTON_L2:     return KeyEvent.KEYCODE_S;
            case KeyEvent.KEYCODE_BUTTON_R2:     return KeyEvent.KEYCODE_D;
            case KeyEvent.KEYCODE_BUTTON_THUMBL: return KeyEvent.KEYCODE_S;
            case KeyEvent.KEYCODE_BUTTON_THUMBR: return KeyEvent.KEYCODE_D;
            case KeyEvent.KEYCODE_BUTTON_START:  return KeyEvent.KEYCODE_ESCAPE;
            case KeyEvent.KEYCODE_BUTTON_SELECT: return -1;
            case KeyEvent.KEYCODE_BUTTON_MODE:   return -1;
            case KeyEvent.KEYCODE_BUTTON_C:      return KeyEvent.KEYCODE_C;
            case KeyEvent.KEYCODE_BUTTON_Z:      return KeyEvent.KEYCODE_D;
            default:                             return keycode;
        }
    }

    public boolean processGamepadEvent(KeyEvent evt)
    {
        InputDevice device = evt.getDevice();

        if (device == null)
            return false;

        int sources = device.getSources();

        if (
            ((sources & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD) &&
            ((sources & InputDevice.SOURCE_DPAD) != InputDevice.SOURCE_DPAD) &&
            !KeyEvent.isGamepadButton(evt.getKeyCode())
        )
            return false;

        int keycode = mapControllerKey(evt.getKeyCode());

        if (keycode < 0)
            return true; // swallowed (e.g. Select)

        if (evt.getRepeatCount() > 0)
            return true; // the game handles held keys itself

        switch (evt.getAction())
        {
            case KeyEvent.ACTION_DOWN:
                mOnKeyDownListener.onKeyDown(keycode);
                break;

            case KeyEvent.ACTION_UP:
                mOnKeyUpListener.onKeyUp(keycode);
                break;
        }

        return true;
    }

    // Analog stick -> arrow keys (Essentia)
    private static final float STICK_THRESHOLD = 0.5f;
    private int mStickX = 0; // -1 left, 0 center, 1 right
    private int mStickY = 0; // -1 up, 0 center, 1 down

    private void updateStickAxis(int oldDir, int newDir, int negKey, int posKey)
    {
        if (oldDir == newDir) return;
        if (oldDir < 0) mOnKeyUpListener.onKeyUp(negKey);
        if (oldDir > 0) mOnKeyUpListener.onKeyUp(posKey);
        if (newDir < 0) mOnKeyDownListener.onKeyDown(negKey);
        if (newDir > 0) mOnKeyDownListener.onKeyDown(posKey);
    }

    public boolean processStickEvent(MotionEvent evt)
    {
        if ((evt.getSource() & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK
            || evt.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        float x = evt.getAxisValue(MotionEvent.AXIS_X);
        float y = evt.getAxisValue(MotionEvent.AXIS_Y);

        int newX = x <= -STICK_THRESHOLD ? -1 : (x >= STICK_THRESHOLD ? 1 : 0);
        int newY = y <= -STICK_THRESHOLD ? -1 : (y >= STICK_THRESHOLD ? 1 : 0);

        updateStickAxis(mStickX, newX, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT);
        updateStickAxis(mStickY, newY, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN);
        mStickX = newX;
        mStickY = newY;

        return true;
    }

    // D-pad reported as hat axes (Essentia: track state, MOVE events carry the changes)
    private int mHatX = 0;
    private int mHatY = 0;

    public boolean processDPadEvent(MotionEvent evt)
    {
        InputDevice device = evt.getDevice();

        if (device == null)
            return false;

        int sources = device.getSources();

        if (((sources & InputDevice.SOURCE_DPAD) != InputDevice.SOURCE_DPAD)
            && ((sources & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK))
            return false;

        float xAxis = evt.getAxisValue(MotionEvent.AXIS_HAT_X);
        float yAxis = evt.getAxisValue(MotionEvent.AXIS_HAT_Y);

        int newX = xAxis <= -0.5f ? -1 : (xAxis >= 0.5f ? 1 : 0);
        int newY = yAxis <= -0.5f ? -1 : (yAxis >= 0.5f ? 1 : 0);

        if (newX == mHatX && newY == mHatY)
            return false;

        updateStickAxis(mHatX, newX, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT);
        updateStickAxis(mHatY, newY, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN);
        mHatX = newX;
        mHatY = newY;

        return true;
    }
}