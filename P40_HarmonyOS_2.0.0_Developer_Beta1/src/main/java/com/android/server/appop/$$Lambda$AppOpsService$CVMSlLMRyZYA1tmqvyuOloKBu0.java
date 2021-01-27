package com.android.server.appop;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.appop.-$$Lambda$AppOpsService$CVMS-lLMRyZYA1tmqvyuOloKBu0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$CVMSlLMRyZYA1tmqvyuOloKBu0 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$CVMSlLMRyZYA1tmqvyuOloKBu0 INSTANCE = new $$Lambda$AppOpsService$CVMSlLMRyZYA1tmqvyuOloKBu0();

    private /* synthetic */ $$Lambda$AppOpsService$CVMSlLMRyZYA1tmqvyuOloKBu0() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppOpsService) obj).updatePendingState(((Long) obj2).longValue(), ((Integer) obj3).intValue());
    }
}
