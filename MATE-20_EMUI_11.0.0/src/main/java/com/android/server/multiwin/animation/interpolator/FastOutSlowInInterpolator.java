package com.android.server.multiwin.animation.interpolator;

import android.view.animation.PathInterpolator;

public class FastOutSlowInInterpolator extends PathInterpolator {
    private static final float X1 = 0.4f;
    private static final float X2 = 0.2f;
    private static final float Y1 = 0.0f;
    private static final float Y2 = 1.0f;

    public FastOutSlowInInterpolator() {
        super(0.4f, 0.0f, 0.2f, 1.0f);
    }
}
