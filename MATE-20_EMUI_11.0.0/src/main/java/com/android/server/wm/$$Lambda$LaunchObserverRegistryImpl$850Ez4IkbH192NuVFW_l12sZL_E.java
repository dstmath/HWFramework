package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E implements BiConsumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((LaunchObserverRegistryImpl) obj).handleUnregisterLaunchObserver((ActivityMetricsLaunchObserver) obj2);
    }
}
