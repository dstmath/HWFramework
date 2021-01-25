package com.android.server.multiwin.animation.interpolator;

import android.view.animation.PathInterpolator;

public class SharpCurveInterpolator extends PathInterpolator {
    private static final float X1 = 0.33f;
    private static final float X2 = 0.67f;
    private static final float Y1 = 0.0f;
    private static final float Y2 = 1.0f;

    public SharpCurveInterpolator() {
        super(0.33f, 0.0f, 0.67f, 1.0f);
    }
}
