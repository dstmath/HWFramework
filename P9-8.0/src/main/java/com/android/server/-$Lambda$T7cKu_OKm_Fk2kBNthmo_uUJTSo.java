package com.android.server;

import android.content.Context;
import com.android.server.input.InputManagerService;
import com.android.server.media.MediaRouterService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.wm.WindowManagerService;

final /* synthetic */ class -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo implements Runnable {

    /* renamed from: com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((SystemServer) this.-$f0).lambda$-com_android_server_SystemServer_99812();
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f10;
        private final /* synthetic */ Object -$f11;
        private final /* synthetic */ Object -$f12;
        private final /* synthetic */ Object -$f13;
        private final /* synthetic */ Object -$f14;
        private final /* synthetic */ Object -$f15;
        private final /* synthetic */ Object -$f16;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;
        private final /* synthetic */ Object -$f8;
        private final /* synthetic */ Object -$f9;

        private final /* synthetic */ void $m$0() {
            ((SystemServer) this.-$f1).lambda$-com_android_server_SystemServer_98907((Context) this.-$f2, (WindowManagerService) this.-$f3, (NetworkScoreService) this.-$f4, (NetworkManagementService) this.-$f5, (NetworkPolicyManagerService) this.-$f6, (NetworkStatsService) this.-$f7, (ConnectivityService) this.-$f8, (LocationManagerService) this.-$f9, (CountryDetectorService) this.-$f10, (NetworkTimeUpdateService) this.-$f11, (CommonTimeManagementService) this.-$f12, (InputManagerService) this.-$f13, (TelephonyRegistry) this.-$f14, (MediaRouterService) this.-$f15, (MmsServiceBroker) this.-$f16, this.-$f0);
        }

        public /* synthetic */ AnonymousClass5(boolean z, Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8, Object obj9, Object obj10, Object obj11, Object obj12, Object obj13, Object obj14, Object obj15, Object obj16) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
            this.-$f4 = obj4;
            this.-$f5 = obj5;
            this.-$f6 = obj6;
            this.-$f7 = obj7;
            this.-$f8 = obj8;
            this.-$f9 = obj9;
            this.-$f10 = obj10;
            this.-$f11 = obj11;
            this.-$f12 = obj12;
            this.-$f13 = obj13;
            this.-$f14 = obj14;
            this.-$f15 = obj15;
            this.-$f16 = obj16;
        }

        public final void run() {
            $m$0();
        }
    }

    public final void run() {
        $m$0();
    }
}
