package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDW-ow  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDWow implements BiConsumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDWow INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDWow();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDWow() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((LaunchObserverRegistryImpl) obj).handleOnActivityLaunchFinished((byte[]) obj2);
    }
}
