package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip.ChipDebugInfo;
import android.hardware.wifi.V1_0.IWifiChip.getDebugHostWakeReasonStatsCallback;
import android.hardware.wifi.V1_0.IWifiChip.getDebugRingBuffersStatusCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestChipDebugInfoCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestDriverDebugDumpCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestFirmwareDebugDumpCallback;
import android.hardware.wifi.V1_0.IWifiRttController.getCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiRttController.getResponderInfoCallback;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIface.getApfPacketFilterCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getBackgroundScanCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getDebugRxPacketFatesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getDebugTxPacketFatesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getLinkLayerStatsCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getRoamingCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getValidFrequenciesForBandCallback;
import android.hardware.wifi.V1_0.RttCapabilities;
import android.hardware.wifi.V1_0.RttResponder;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanCapabilities;
import android.hardware.wifi.V1_0.StaLinkLayerStats;
import android.hardware.wifi.V1_0.StaRoamingCapabilities;
import android.hardware.wifi.V1_0.WifiDebugHostWakeReasonStats;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiStatus;
import android.util.MutableBoolean;
import android.util.MutableInt;
import com.android.server.wifi.WifiNative.RoamingCapabilities;
import com.android.server.wifi.WifiNative.RxFateReport;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.TxFateReport;
import com.android.server.wifi.WifiVendorHal.AnonymousClass1AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass2AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass3AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass4AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass5AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass6AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass7AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass8AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass9AnswerBox;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA implements requestChipDebugInfoCallback {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$10 */
    final /* synthetic */ class AnonymousClass10 implements getLinkLayerStatsCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaLinkLayerStats arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_25591((AnonymousClass1AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats) {
            $m$0(wifiStatus, staLinkLayerStats);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$11 */
    final /* synthetic */ class AnonymousClass11 implements getValidFrequenciesForBandCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_54636((AnonymousClass4AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$12 */
    final /* synthetic */ class AnonymousClass12 implements getBackgroundScanCapabilitiesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaBackgroundScanCapabilities arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_16028((ScanCapabilities) this.-$f1, (MutableBoolean) this.-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities) {
            $m$0(wifiStatus, staBackgroundScanCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$13 */
    final /* synthetic */ class AnonymousClass13 implements getDebugRxPacketFatesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_75983((RxFateReport[]) this.-$f1, (MutableBoolean) this.-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$14 */
    final /* synthetic */ class AnonymousClass14 implements getDebugTxPacketFatesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_74196((TxFateReport[]) this.-$f1, (MutableBoolean) this.-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$15 */
    final /* synthetic */ class AnonymousClass15 implements getRoamingCapabilitiesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaRoamingCapabilities arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_85051((RoamingCapabilities) this.-$f1, (MutableBoolean) this.-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities) {
            $m$0(wifiStatus, staRoamingCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$16 */
    final /* synthetic */ class AnonymousClass16 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((ChipEventCallback) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal$ChipEventCallback_95528((WifiDebugRingBufferStatus) this.-$f1, (ArrayList) this.-$f2);
        }

        public /* synthetic */ AnonymousClass16(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$17 */
    final /* synthetic */ class AnonymousClass17 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((ChipEventCallback) this.-$f1).lambda$-com_android_server_wifi_WifiVendorHal$ChipEventCallback_97191((ArrayList) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass17(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$1 */
    final /* synthetic */ class AnonymousClass1 implements IWifiApIface.getValidFrequenciesForBandCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_55149((AnonymousClass4AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$2 */
    final /* synthetic */ class AnonymousClass2 implements getDebugHostWakeReasonStatsCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, WifiDebugHostWakeReasonStats arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_83424((AnonymousClass9AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, WifiDebugHostWakeReasonStats wifiDebugHostWakeReasonStats) {
            $m$0(wifiStatus, wifiDebugHostWakeReasonStats);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$3 */
    final /* synthetic */ class AnonymousClass3 implements getDebugRingBuffersStatusCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_67364((AnonymousClass6AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$4 */
    final /* synthetic */ class AnonymousClass4 implements requestDriverDebugDumpCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_69329((AnonymousClass8AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$5 */
    final /* synthetic */ class AnonymousClass5 implements requestFirmwareDebugDumpCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_68635((AnonymousClass7AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$6 */
    final /* synthetic */ class AnonymousClass6 implements getCapabilitiesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, RttCapabilities arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_33500((AnonymousClass2AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities) {
            $m$0(wifiStatus, rttCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$7 */
    final /* synthetic */ class AnonymousClass7 implements getResponderInfoCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, RttResponder arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_49882((AnonymousClass3AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, RttResponder rttResponder) {
            $m$0(wifiStatus, rttResponder);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$8 */
    final /* synthetic */ class AnonymousClass8 implements getApfPacketFilterCapabilitiesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaApfPacketFilterCapabilities arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_57159((AnonymousClass5AnswerBox) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities) {
            $m$0(wifiStatus, staApfPacketFilterCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$9 */
    final /* synthetic */ class AnonymousClass9 implements IWifiStaIface.getCapabilitiesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_32009((MutableInt) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    private final /* synthetic */ void $m$0(WifiStatus arg0, ChipDebugInfo arg1) {
        ((WifiVendorHal) this.-$f0).lambda$-com_android_server_wifi_WifiVendorHal_64421(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA(Object obj) {
        this.-$f0 = obj;
    }

    public final void onValues(WifiStatus wifiStatus, ChipDebugInfo chipDebugInfo) {
        $m$0(wifiStatus, chipDebugInfo);
    }
}
