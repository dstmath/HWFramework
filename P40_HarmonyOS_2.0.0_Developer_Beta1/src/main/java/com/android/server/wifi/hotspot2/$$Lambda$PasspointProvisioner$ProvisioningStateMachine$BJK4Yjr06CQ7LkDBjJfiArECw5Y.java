package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.hotspot2.-$$Lambda$PasspointProvisioner$ProvisioningStateMachine$BJK4Yjr06CQ7LkDBjJfiArECw5Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PasspointProvisioner$ProvisioningStateMachine$BJK4Yjr06CQ7LkDBjJfiArECw5Y implements Predicate {
    public static final /* synthetic */ $$Lambda$PasspointProvisioner$ProvisioningStateMachine$BJK4Yjr06CQ7LkDBjJfiArECw5Y INSTANCE = new $$Lambda$PasspointProvisioner$ProvisioningStateMachine$BJK4Yjr06CQ7LkDBjJfiArECw5Y();

    private /* synthetic */ $$Lambda$PasspointProvisioner$ProvisioningStateMachine$BJK4Yjr06CQ7LkDBjJfiArECw5Y() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ScanResult) obj).isPasspointNetwork();
    }
}
