package com.android.server.am;

import java.util.concurrent.ThreadFactory;

/* renamed from: com.android.server.am.-$$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg implements ThreadFactory {
    public static final /* synthetic */ $$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg INSTANCE = new $$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg();

    private /* synthetic */ $$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg() {
    }

    public final Thread newThread(Runnable runnable) {
        return BatteryExternalStatsWorker.lambda$new$0(runnable);
    }
}
