package com.android.server.wm;

import android.os.IRemoteCallback;
import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU implements Consumer {
    public static final /* synthetic */ $$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU INSTANCE = new $$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU();

    private /* synthetic */ $$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        AppTransition.doAnimationCallback((IRemoteCallback) obj);
    }
}
