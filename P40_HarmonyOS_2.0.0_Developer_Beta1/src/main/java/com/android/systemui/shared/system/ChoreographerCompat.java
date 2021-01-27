package com.android.systemui.shared.system;

import android.view.Choreographer;

public class ChoreographerCompat {
    public static void postInputFrame(Choreographer choreographer, Runnable runnable) {
        choreographer.postCallback(0, runnable, null);
    }

    public static Choreographer getSfInstance() {
        return Choreographer.getSfInstance();
    }
}
