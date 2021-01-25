package com.android.server;

import com.android.internal.os.LooperStats;
import java.util.function.Function;

/* renamed from: com.android.server.-$$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ implements Function {
    public static final /* synthetic */ $$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ INSTANCE = new $$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ();

    private /* synthetic */ $$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((LooperStats.ExportedEntry) obj).threadName;
    }
}
