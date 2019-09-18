package android.os;

import android.opengl.EGL14;

/* renamed from: android.os.-$$Lambda$GraphicsEnvironment$U4RqBlx5-Js31-71IFOgvpvoAFg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GraphicsEnvironment$U4RqBlx5Js3171IFOgvpvoAFg implements Runnable {
    public static final /* synthetic */ $$Lambda$GraphicsEnvironment$U4RqBlx5Js3171IFOgvpvoAFg INSTANCE = new $$Lambda$GraphicsEnvironment$U4RqBlx5Js3171IFOgvpvoAFg();

    private /* synthetic */ $$Lambda$GraphicsEnvironment$U4RqBlx5Js3171IFOgvpvoAFg() {
    }

    public final void run() {
        EGL14.eglGetDisplay(0);
    }
}
