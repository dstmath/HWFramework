package com.android.server.am;

import java.util.concurrent.ThreadFactory;

/* renamed from: com.android.server.am.-$$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w implements ThreadFactory {
    public static final /* synthetic */ $$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w INSTANCE = new $$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w();

    private /* synthetic */ $$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w() {
    }

    @Override // java.util.concurrent.ThreadFactory
    public final Thread newThread(Runnable runnable) {
        return BatteryExternalStatsWorker.lambda$new$1(runnable);
    }
}
