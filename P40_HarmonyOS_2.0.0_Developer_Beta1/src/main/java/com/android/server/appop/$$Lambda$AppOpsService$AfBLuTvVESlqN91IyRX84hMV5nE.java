package com.android.server.appop;

import android.util.ArraySet;
import com.android.internal.util.function.HexConsumer;

/* renamed from: com.android.server.appop.-$$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE implements HexConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE INSTANCE = new $$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE();

    private /* synthetic */ $$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((AppOpsService) obj).notifyOpChecked((ArraySet) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (String) obj5, ((Integer) obj6).intValue());
    }
}
