package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks implements Consumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((LaunchObserverRegistryImpl) obj).handleOnIntentFailed();
    }
}
