package com.android.systemui.shared.system;

import android.view.ThreadedRenderer;

public class ThreadedRendererCompat {
    public static final int EGL_CONTEXT_PRIORITY_HIGH_IMG = 12545;
    public static final int EGL_CONTEXT_PRIORITY_LOW_IMG = 12547;
    public static final int EGL_CONTEXT_PRIORITY_MEDIUM_IMG = 12546;

    public static void setContextPriority(int priority) {
        ThreadedRenderer.setContextPriority(priority);
    }
}
