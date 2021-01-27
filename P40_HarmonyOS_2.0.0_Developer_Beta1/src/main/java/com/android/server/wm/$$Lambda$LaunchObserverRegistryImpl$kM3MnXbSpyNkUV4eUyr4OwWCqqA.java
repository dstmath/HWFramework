package com.android.server.wm;

import android.content.Intent;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA implements BiConsumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((LaunchObserverRegistryImpl) obj).handleOnIntentStarted((Intent) obj2);
    }
}
