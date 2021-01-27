package com.android.server.wm;

import android.os.IRemoteCallback;
import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4 implements Consumer {
    public static final /* synthetic */ $$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4 INSTANCE = new $$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4();

    private /* synthetic */ $$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        AppTransition.doAnimationCallback((IRemoteCallback) obj);
    }
}
