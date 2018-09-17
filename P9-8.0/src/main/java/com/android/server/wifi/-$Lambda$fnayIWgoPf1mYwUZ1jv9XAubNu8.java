package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.addNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.listNetworksCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getMacAddressCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.startWpsPinDisplayCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.IHwBinder.DeathRecipient;
import java.util.ArrayList;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.VowifiDetectCallback;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getApVendorInfoCallback;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getCapabRsdbCallback;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getMssStateCallback;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getWpasConfigCallback;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.heartBeatCallback;

final /* synthetic */ class -$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8 implements getInterfaceCallback {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$10 */
    final /* synthetic */ class AnonymousClass10 implements getApVendorInfoCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_95037((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$11 */
    final /* synthetic */ class AnonymousClass11 implements getCapabRsdbCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_91688((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$12 */
    final /* synthetic */ class AnonymousClass12 implements getMssStateCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_94152((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$13 */
    final /* synthetic */ class AnonymousClass13 implements getWpasConfigCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_92578((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$14 */
    final /* synthetic */ class AnonymousClass14 implements heartBeatCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_86813((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$1 */
    final /* synthetic */ class AnonymousClass1 implements listInterfacesCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantStaIfaceHal.lambda$-com_android_server_wifi_SupplicantStaIfaceHal_11493((ArrayList) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$2 */
    final /* synthetic */ class AnonymousClass2 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_5549(arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$3 */
    final /* synthetic */ class AnonymousClass3 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_5939(arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$4 */
    final /* synthetic */ class AnonymousClass4 implements addNetworkCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_27566((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$5 */
    final /* synthetic */ class AnonymousClass5 implements getNetworkCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_30094((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$6 */
    final /* synthetic */ class AnonymousClass6 implements listNetworksCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_31873((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$7 */
    final /* synthetic */ class AnonymousClass7 implements getMacAddressCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_48327((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$8 */
    final /* synthetic */ class AnonymousClass8 implements startWpsPinDisplayCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_61926((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$9 */
    final /* synthetic */ class AnonymousClass9 implements VowifiDetectCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.-$f0).lambda$-com_android_server_wifi_SupplicantStaIfaceHal_85873((Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantIface arg1) {
        SupplicantStaIfaceHal.lambda$-com_android_server_wifi_SupplicantStaIfaceHal_12604((Mutable) this.-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8(Object obj) {
        this.-$f0 = obj;
    }

    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
        $m$0(supplicantStatus, iSupplicantIface);
    }
}
