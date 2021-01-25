package com.android.server.wm;

import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$VY87MmFWaCLMkNa2qHGaPrThyrI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VY87MmFWaCLMkNa2qHGaPrThyrI implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$VY87MmFWaCLMkNa2qHGaPrThyrI INSTANCE = new $$Lambda$VY87MmFWaCLMkNa2qHGaPrThyrI();

    private /* synthetic */ $$Lambda$VY87MmFWaCLMkNa2qHGaPrThyrI() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((WindowProcessListener) obj).onStartActivity(((Integer) obj2).intValue(), ((Boolean) obj3).booleanValue(), (String) obj4, ((Long) obj5).longValue());
    }
}
