package com.android.server.wifi.wifinearfind;

import android.media.AudioRecordingConfiguration;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.wifinearfind.-$$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s implements Predicate {
    public static final /* synthetic */ $$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s INSTANCE = new $$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s();

    private /* synthetic */ $$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return HwWifiNearFindMonitor.lambda$isRecordingInVoip$0((AudioRecordingConfiguration) obj);
    }
}
