package com.android.server.appop;

import android.util.ArraySet;
import com.android.internal.util.function.HexConsumer;

/* renamed from: com.android.server.appop.-$$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ implements HexConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ INSTANCE = new $$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ();

    private /* synthetic */ $$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((AppOpsService) obj).notifyOpActiveChanged((ArraySet) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (String) obj5, ((Boolean) obj6).booleanValue());
    }
}
