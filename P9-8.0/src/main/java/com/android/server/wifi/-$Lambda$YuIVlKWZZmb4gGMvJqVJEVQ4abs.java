package com.android.server.wifi;

import com.android.server.wifi.WifiNative.VendorHalDeathEventHandler;

final /* synthetic */ class -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs implements VendorHalDeathEventHandler {
    private final /* synthetic */ Object -$f0;

    private final /* synthetic */ void $m$0() {
        ((WifiStateMachine) this.-$f0).lambda$-com_android_server_wifi_WifiStateMachine_14512();
    }

    public /* synthetic */ -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs(Object obj) {
        this.-$f0 = obj;
    }

    public final void onDeath() {
        $m$0();
    }
}
