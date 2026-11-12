package com.hatkid.mkxpz;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

/**
 * The colors of the palette the player chose in Gahvoria, for the game screen's own views (loading screen,
 * dialogs, notices). The app passes them in the intent; without them it uses graphite and coral.
 */
final class Look
{
    final int bg, bg2, ink, muted, faint, accent, accent2, accent3, onAccent, ok, warn, err, line;
    final boolean light;
    private final float density;

    Look(Context c, int[] a)
    {
        boolean has = a != null && a.length >= 10;
        bg = has ? a[0] : 0xFF0D0E12;
        bg2 = has ? a[1] : 0xFF1C1D26;
        ink = has ? a[2] : 0xFFF2F2F6;
        muted = has ? a[3] : 0xFF9C9DB0;
        accent = has ? a[4] : 0xFFFF7A6B;
        accent2 = has ? a[5] : 0xFFFFB38A;
        accent3 = has ? a[6] : 0xFFA9B4FF;
        onAccent = has ? a[7] : 0xFF1A0B09;
        ok = has ? a[8] : 0xFF7ED6A8;
        warn = has ? a[9] : 0xFFFFB38A;
        light = luminance(bg) > 0.6;
        err = light ? 0xFFC62F3A : 0xFFFF8E8E;
        faint = withAlpha(muted, 0xB8);
        line = light ? 0x331E1E3C : 0x1AFFFFFF;
        density = c.getResources().getDisplayMetrics().density;
    }

    int dp(float v)
    {
        return Math.round(v * density);
    }

    static int withAlpha(int color, int alpha)
    {
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    private static double luminance(int c)
    {
        return (0.299 * ((c >> 16) & 0xFF) + 0.587 * ((c >> 8) & 0xFF) + 0.114 * (c & 0xFF)) / 255.0;
    }

    /** A rounded box, with an optional 1 dp border (0 = none). */
    GradientDrawable box(int fill, float radiusDp, int stroke)
    {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    /** The coral gradient of the main button. */
    GradientDrawable gradient(float radiusDp)
    {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{accent, accent2, accent3});
        g.setCornerRadius(dp(radiusDp));
        return g;
    }
}
