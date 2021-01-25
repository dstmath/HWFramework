package com.android.server.appop;

import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.appop.-$$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0 INSTANCE = new $$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0();

    private /* synthetic */ $$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((AppOpsService) obj).notifyOpChangedForAllPkgsInUid(((Integer) obj2).intValue(), ((Integer) obj3).intValue(), ((Boolean) obj4).booleanValue());
    }
}
