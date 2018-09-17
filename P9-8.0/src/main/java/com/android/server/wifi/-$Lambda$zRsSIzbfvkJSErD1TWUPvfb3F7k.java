package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifi.getChipCallback;
import android.hardware.wifi.V1_0.IWifi.getChipIdsCallback;
import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChip.createApIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createNanIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createP2pIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createRttControllerCallback;
import android.hardware.wifi.V1_0.IWifiChip.createStaIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getApIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getApIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getAvailableModesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getIdCallback;
import android.hardware.wifi.V1_0.IWifiChip.getModeCallback;
import android.hardware.wifi.V1_0.IWifiChip.getNanIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getNanIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getStaIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getStaIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiIface.getNameCallback;
import android.hardware.wifi.V1_0.IWifiIface.getTypeCallback;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.os.IHwBinder.DeathRecipient;
import android.util.MutableBoolean;
import android.util.MutableInt;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k implements createRttControllerCallback {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$10 */
    final /* synthetic */ class AnonymousClass10 implements createNanIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiNanIface arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_70422((Mutable) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
            $m$0(wifiStatus, iWifiNanIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$11 */
    final /* synthetic */ class AnonymousClass11 implements createP2pIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiP2pIface arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_70041((Mutable) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
            $m$0(wifiStatus, iWifiP2pIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$12 */
    final /* synthetic */ class AnonymousClass12 implements createStaIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiStaIface arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_69282((Mutable) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
            $m$0(wifiStatus, iWifiStaIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$13 */
    final /* synthetic */ class AnonymousClass13 implements getApIfaceNamesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_36863((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$14 */
    final /* synthetic */ class AnonymousClass14 implements getAvailableModesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_33339((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$15 */
    final /* synthetic */ class AnonymousClass15 implements getIdCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_51432((MutableInt) this.-$f0, (MutableBoolean) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$16 */
    final /* synthetic */ class AnonymousClass16 implements getNanIfaceNamesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_40469((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$17 */
    final /* synthetic */ class AnonymousClass17 implements getP2pIfaceNamesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_38663((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass17(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$18 */
    final /* synthetic */ class AnonymousClass18 implements getStaIfaceNamesCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_35058((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass18(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$19 */
    final /* synthetic */ class AnonymousClass19 implements getModeCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_34120((MutableBoolean) this.-$f0, (MutableBoolean) this.-$f1, (MutableInt) this.-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass19(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$1 */
    final /* synthetic */ class AnonymousClass1 implements getNameCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(WifiStatus arg0, String arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_13199((Mutable) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(WifiStatus wifiStatus, String str) {
            $m$0(wifiStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$20 */
    final /* synthetic */ class AnonymousClass20 implements getApIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiApIface arg1) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_37689((MutableBoolean) this.-$f1, (String) this.-$f2, (WifiIfaceInfo[]) this.-$f3, (MutableInt) this.-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass20(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
            $m$0(wifiStatus, iWifiApIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$21 */
    final /* synthetic */ class AnonymousClass21 implements getNanIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiNanIface arg1) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_41298((MutableBoolean) this.-$f1, (String) this.-$f2, (WifiIfaceInfo[]) this.-$f3, (MutableInt) this.-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass21(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
            $m$0(wifiStatus, iWifiNanIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$22 */
    final /* synthetic */ class AnonymousClass22 implements getP2pIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiP2pIface arg1) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_39492((MutableBoolean) this.-$f1, (String) this.-$f2, (WifiIfaceInfo[]) this.-$f3, (MutableInt) this.-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass22(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
            $m$0(wifiStatus, iWifiP2pIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$23 */
    final /* synthetic */ class AnonymousClass23 implements getStaIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiStaIface arg1) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_35887((MutableBoolean) this.-$f1, (String) this.-$f2, (WifiIfaceInfo[]) this.-$f3, (MutableInt) this.-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass23(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
            $m$0(wifiStatus, iWifiStaIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$2 */
    final /* synthetic */ class AnonymousClass2 implements getTypeCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_79875((MutableInt) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$3 */
    final /* synthetic */ class AnonymousClass3 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_20381(arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$4 */
    final /* synthetic */ class AnonymousClass4 implements DeathRecipient {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((HalDeviceManager) this.-$f0).lambda$-com_android_server_wifi_HalDeviceManager_23995(arg0);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void serviceDied(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$5 */
    final /* synthetic */ class AnonymousClass5 implements getChipCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiChip arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_32673((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
            $m$0(wifiStatus, iWifiChip);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$6 */
    final /* synthetic */ class AnonymousClass6 implements getChipCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiChip arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_27706((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
            $m$0(wifiStatus, iWifiChip);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$7 */
    final /* synthetic */ class AnonymousClass7 implements getChipIdsCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_31674((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$8 */
    final /* synthetic */ class AnonymousClass8 implements getChipIdsCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_26791((MutableBoolean) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$9 */
    final /* synthetic */ class AnonymousClass9 implements createApIfaceCallback {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiApIface arg1) {
            HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_69661((Mutable) this.-$f0, (Mutable) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
            $m$0(wifiStatus, iWifiApIface);
        }
    }

    private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiRttController arg1) {
        HalDeviceManager.lambda$-com_android_server_wifi_HalDeviceManager_15675((Mutable) this.-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k(Object obj) {
        this.-$f0 = obj;
    }

    public final void onValues(WifiStatus wifiStatus, IWifiRttController iWifiRttController) {
        $m$0(wifiStatus, iWifiRttController);
    }
}
