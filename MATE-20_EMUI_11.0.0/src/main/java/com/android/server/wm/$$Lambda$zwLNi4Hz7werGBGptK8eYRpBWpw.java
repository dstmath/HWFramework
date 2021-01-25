package com.android.server.wm;

import android.app.ActivityManagerInternal;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw INSTANCE = new $$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw();

    private /* synthetic */ $$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityManagerInternal) obj).reportCurKeyguardUsageEvent(((Boolean) obj2).booleanValue());
    }
}
