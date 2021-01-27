package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.content.ComponentName;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$hT1kyMEAhvB1-Uxr0DFAlnuU3cQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ INSTANCE = new $$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ();

    private /* synthetic */ $$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((ActivityManagerInternal) obj).updateBatteryStats((ComponentName) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), ((Boolean) obj5).booleanValue());
    }
}
