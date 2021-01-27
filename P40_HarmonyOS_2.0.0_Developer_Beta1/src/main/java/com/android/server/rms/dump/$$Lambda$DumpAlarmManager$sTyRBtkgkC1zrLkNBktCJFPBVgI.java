package com.android.server.rms.dump;

import com.android.server.rms.dump.DumpAlarmManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.rms.dump.-$$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI INSTANCE = new $$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI();

    private /* synthetic */ $$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAlarmManager.Params params;
        DumpAlarmManager.delay(params.context, params.pw, ((DumpAlarmManager.Params) obj).args);
    }
}
