package com.android.server.appop;

import android.util.ArraySet;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.appop.-$$Lambda$AppOpsService$NDUi03ZZuuR42-RDEIQ0UELKycc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$NDUi03ZZuuR42RDEIQ0UELKycc implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$NDUi03ZZuuR42RDEIQ0UELKycc INSTANCE = new $$Lambda$AppOpsService$NDUi03ZZuuR42RDEIQ0UELKycc();

    private /* synthetic */ $$Lambda$AppOpsService$NDUi03ZZuuR42RDEIQ0UELKycc() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((AppOpsService) obj).notifyOpChanged((ArraySet) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (String) obj5);
    }
}
