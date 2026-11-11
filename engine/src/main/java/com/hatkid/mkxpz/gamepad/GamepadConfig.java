package com.hatkid.mkxpz.gamepad;

import android.view.KeyEvent;

public class GamepadConfig
{
    /** In-screen gamepad settings (Essentia: set from EssentiaSettings) **/

    // Opacity of view elements in percentage
    public Integer opacity = 40;

    // View elements scale in percentage
    public Integer scale = 100;

    // Whether use diagonal (8-way) movement on D-Pad (default: false)
    public Boolean diagonalMovement = false;

    /**
     * Key bindings for each on-screen button. Essentia uses the same keys as the
     * physical controller mapping, so A always confirms and B always cancels:
     *   A = Enter (confirm)   B = Esc (cancel / menu)
     *   X = Shift             Y = A key       Z = D key      C = S key
     *   L = Q (page up)       R = W (page down)
     *   bottom row: F1 (controls screen in Essentials), MENU (Esc), SHIFT
     */
    public final Integer keycodeA = KeyEvent.KEYCODE_ENTER;
    public final Integer keycodeB = KeyEvent.KEYCODE_ESCAPE;
    public final Integer keycodeC = KeyEvent.KEYCODE_S;
    public final Integer keycodeX = KeyEvent.KEYCODE_SHIFT_LEFT;
    public final Integer keycodeY = KeyEvent.KEYCODE_A;
    public final Integer keycodeZ = KeyEvent.KEYCODE_D;
    public final Integer keycodeL = KeyEvent.KEYCODE_Q;
    public final Integer keycodeR = KeyEvent.KEYCODE_W;
    public final Integer keycodeCTRL = KeyEvent.KEYCODE_F1;
    public final Integer keycodeALT = KeyEvent.KEYCODE_ESCAPE;
    public final Integer keycodeSHIFT = KeyEvent.KEYCODE_SHIFT_LEFT;

    /** Labels shown on the buttons (by position, not by key). */
    public final String labelA = "A";
    public final String labelB = "B";
    public final String labelC = "C";
    public final String labelX = "X";
    public final String labelY = "Y";
    public final String labelZ = "Z";
    public final String labelL = "L";
    public final String labelR = "R";
    public final String labelCTRL = "F1";
    public final String labelALT = "MENÚ";
    public final String labelSHIFT = "SHIFT";
}
