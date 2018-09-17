package com.android.server.pm;

import android.content.IntentSender;
import android.content.pm.IPackageDataObserver;
import java.util.List;

final /* synthetic */ class -$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;
    private final /* synthetic */ Object -$f2;

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ void $m$0() {
            PackageManagerService.lambda$-com_android_server_pm_PackageManagerService_1044020(this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i) {
            this.-$f0 = i;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((PackageManagerService) this.-$f1).lambda$-com_android_server_pm_PackageManagerService_158110((List) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ long -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0() {
            ((PackageManagerService) this.-$f2).lambda$-com_android_server_pm_PackageManagerService_221240((String) this.-$f3, this.-$f1, this.-$f0, (IntentSender) this.-$f4);
        }

        public /* synthetic */ AnonymousClass3(int i, long j, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = j;
            this.-$f2 = obj;
            this.-$f3 = obj2;
            this.-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ long -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0() {
            ((PackageManagerService) this.-$f2).lambda$-com_android_server_pm_PackageManagerService_220441((String) this.-$f3, this.-$f1, this.-$f0, (IPackageDataObserver) this.-$f4);
        }

        public /* synthetic */ AnonymousClass4(int i, long j, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = j;
            this.-$f2 = obj;
            this.-$f3 = obj2;
            this.-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((PackageManagerService) this.-$f0).lambda$-com_android_server_pm_PackageManagerService_740698((int[]) this.-$f1, (String) this.-$f2);
    }

    public /* synthetic */ -$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E(Object obj, Object obj2, Object obj3) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
        this.-$f2 = obj3;
    }

    public final void run() {
        $m$0();
    }
}
