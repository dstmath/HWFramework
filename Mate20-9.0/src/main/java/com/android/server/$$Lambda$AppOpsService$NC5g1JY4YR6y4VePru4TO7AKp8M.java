package com.android.server;

import android.util.ArraySet;
import com.android.internal.util.function.HexConsumer;

/* renamed from: com.android.server.-$$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M implements HexConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M INSTANCE = new $$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M();

    private /* synthetic */ $$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((AppOpsService) obj).notifyOpActiveChanged((ArraySet) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (String) obj5, ((Boolean) obj6).booleanValue());
    }
}
