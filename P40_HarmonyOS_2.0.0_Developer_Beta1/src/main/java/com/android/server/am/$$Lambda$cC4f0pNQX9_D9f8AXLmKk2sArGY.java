package com.android.server.am;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.am.-$$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY implements TriConsumer {
    public static final /* synthetic */ $$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY INSTANCE = new $$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY();

    private /* synthetic */ $$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((BatteryStatsImpl) obj).updateProcStateCpuTimes(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue());
    }
}
