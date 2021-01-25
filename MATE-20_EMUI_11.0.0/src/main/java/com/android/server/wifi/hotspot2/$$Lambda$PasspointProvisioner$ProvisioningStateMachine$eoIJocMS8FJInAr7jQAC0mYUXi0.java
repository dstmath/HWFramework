package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult;
import com.android.server.wifi.hotspot2.PasspointProvisioner;
import java.util.Comparator;

/* renamed from: com.android.server.wifi.hotspot2.-$$Lambda$PasspointProvisioner$ProvisioningStateMachine$eoIJocMS8FJInAr7jQAC0mYUXi0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PasspointProvisioner$ProvisioningStateMachine$eoIJocMS8FJInAr7jQAC0mYUXi0 implements Comparator {
    public static final /* synthetic */ $$Lambda$PasspointProvisioner$ProvisioningStateMachine$eoIJocMS8FJInAr7jQAC0mYUXi0 INSTANCE = new $$Lambda$PasspointProvisioner$ProvisioningStateMachine$eoIJocMS8FJInAr7jQAC0mYUXi0();

    private /* synthetic */ $$Lambda$PasspointProvisioner$ProvisioningStateMachine$eoIJocMS8FJInAr7jQAC0mYUXi0() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return PasspointProvisioner.ProvisioningStateMachine.lambda$getBestMatchingOsuProvider$2((ScanResult) obj, (ScanResult) obj2);
    }
}
