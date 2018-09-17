package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNameCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.listNetworksCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.connectCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.createNfcHandoverRequestMessageCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.createNfcHandoverSelectMessageCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getDeviceAddressCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getGroupCapabilityCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getSsidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.requestServiceDiscoveryCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.startWpsPinDisplayCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.getBssidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.getClientListCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.isCurrentCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.isGoCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.IHwBinder.DeathRecipient;
import java.util.ArrayList;
import java.util.function.Function;

final /* synthetic */ class -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo implements Function {

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$10 */
    final /* synthetic */ class AnonymousClass10 implements getGroupCapabilityCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, Integer.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass10(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$11 */
    final /* synthetic */ class AnonymousClass11 implements getSsidCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantP2pIfaceHal.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_36845((SupplicantResult) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$12 */
    final /* synthetic */ class AnonymousClass12 implements requestServiceDiscoveryCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, long arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, new Long(arg1));
        }

        public /* synthetic */ AnonymousClass12(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, long j) {
            $m$0(supplicantStatus, j);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$13 */
    final /* synthetic */ class AnonymousClass13 implements startWpsPinDisplayCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$14 */
    final /* synthetic */ class AnonymousClass14 implements getBssidCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$15 */
    final /* synthetic */ class AnonymousClass15 implements getClientListCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$16 */
    final /* synthetic */ class AnonymousClass16 implements ISupplicantP2pNetwork.getSsidCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$17 */
    final /* synthetic */ class AnonymousClass17 implements isCurrentCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, Boolean.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass17(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$18 */
    final /* synthetic */ class AnonymousClass18 implements isGoCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, Boolean.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass18(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$19 */
    final /* synthetic */ class AnonymousClass19 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SupplicantP2pIfaceHal) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_4088(arg0);
        }

        public /* synthetic */ AnonymousClass19(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$1 */
    final /* synthetic */ class AnonymousClass1 implements getInterfaceCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantIface arg1) {
            SupplicantP2pIfaceHal.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_10697((SupplicantResult) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
            $m$0(supplicantStatus, iSupplicantIface);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$20 */
    final /* synthetic */ class AnonymousClass20 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SupplicantP2pIfaceHal) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_4478(arg0);
        }

        public /* synthetic */ AnonymousClass20(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$2 */
    final /* synthetic */ class AnonymousClass2 implements listInterfacesCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantP2pIfaceHal.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_9591((ArrayList) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$3 */
    final /* synthetic */ class AnonymousClass3 implements getNameCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$4 */
    final /* synthetic */ class AnonymousClass4 implements getNetworkCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$5 */
    final /* synthetic */ class AnonymousClass5 implements listNetworksCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$6 */
    final /* synthetic */ class AnonymousClass6 implements connectCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$7 */
    final /* synthetic */ class AnonymousClass7 implements createNfcHandoverRequestMessageCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$8 */
    final /* synthetic */ class AnonymousClass8 implements createNfcHandoverSelectMessageCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$9 */
    final /* synthetic */ class AnonymousClass9 implements getDeviceAddressCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            SupplicantP2pIfaceHal.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_35213((SupplicantResult) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}
