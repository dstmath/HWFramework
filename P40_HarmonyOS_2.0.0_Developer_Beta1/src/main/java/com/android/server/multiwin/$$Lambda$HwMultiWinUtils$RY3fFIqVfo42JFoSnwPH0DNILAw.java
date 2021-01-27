package com.android.server.multiwin;

import java.util.concurrent.ThreadFactory;

/* renamed from: com.android.server.multiwin.-$$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw implements ThreadFactory {
    public static final /* synthetic */ $$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw INSTANCE = new $$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw();

    private /* synthetic */ $$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw() {
    }

    @Override // java.util.concurrent.ThreadFactory
    public final Thread newThread(Runnable runnable) {
        return HwMultiWinUtils.lambda$blurForScreenShot$0(runnable);
    }
}
