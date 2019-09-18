package com.android.server.am;

import com.android.internal.os.ProcessCpuTracker;

/* renamed from: com.android.server.am.-$$Lambda$ActivityManagerService$dLQ66dH4nIti4hweaVJTGHj2tMU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityManagerService$dLQ66dH4nIti4hweaVJTGHj2tMU implements ProcessCpuTracker.FilterStats {
    public static final /* synthetic */ $$Lambda$ActivityManagerService$dLQ66dH4nIti4hweaVJTGHj2tMU INSTANCE = new $$Lambda$ActivityManagerService$dLQ66dH4nIti4hweaVJTGHj2tMU();

    private /* synthetic */ $$Lambda$ActivityManagerService$dLQ66dH4nIti4hweaVJTGHj2tMU() {
    }

    public final boolean needed(ProcessCpuTracker.Stats stats) {
        return ActivityManagerService.lambda$reportMemUsage$4(stats);
    }
}
