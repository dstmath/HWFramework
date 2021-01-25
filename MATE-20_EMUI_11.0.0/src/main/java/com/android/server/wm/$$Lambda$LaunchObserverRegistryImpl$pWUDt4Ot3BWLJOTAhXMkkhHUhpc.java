package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((LaunchObserverRegistryImpl) obj).handleRegisterLaunchObserver((ActivityMetricsLaunchObserver) obj2);
    }
}
