package com.android.server;

import android.net.Network;

final /* synthetic */ class -$Lambda$UMiM-Tf_wXGw9kQrVQXa-uniNcc implements Runnable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.-$Lambda$UMiM-Tf_wXGw9kQrVQXa-uniNcc$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((ConnectivityService) this.-$f0).lambda$-com_android_server_ConnectivityService_125386((Network) this.-$f1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((ConnectivityService) this.-$f0).lambda$-com_android_server_ConnectivityService_39129();
    }

    public /* synthetic */ -$Lambda$UMiM-Tf_wXGw9kQrVQXa-uniNcc(Object obj) {
        this.-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
