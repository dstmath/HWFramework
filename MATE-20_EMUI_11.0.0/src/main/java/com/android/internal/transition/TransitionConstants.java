package com.android.internal.transition;

import android.animation.TimeInterpolator;
import android.view.animation.PathInterpolator;

class TransitionConstants {
    static final TimeInterpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    static final TimeInterpolator LINEAR_OUT_SLOW_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);

    TransitionConstants() {
    }
}
