package com.android.server;

import android.util.ArraySet;
import com.android.internal.util.function.QuintConsumer;
import com.android.server.AppOpsService;

/* renamed from: com.android.server.-$$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04 implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04 INSTANCE = new $$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04();

    private /* synthetic */ $$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((AppOpsService) obj).notifyOpChanged((ArraySet<AppOpsService.ModeCallback>) (ArraySet) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (String) obj5);
    }
}
