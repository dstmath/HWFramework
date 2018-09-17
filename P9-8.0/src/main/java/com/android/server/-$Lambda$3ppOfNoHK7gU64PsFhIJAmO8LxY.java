package com.android.server;

import android.app.admin.PasswordMetrics;

final /* synthetic */ class -$Lambda$3ppOfNoHK7gU64PsFhIJAmO8LxY implements Runnable {
    private final /* synthetic */ int -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: com.android.server.-$Lambda$3ppOfNoHK7gU64PsFhIJAmO8LxY$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((LockSettingsService) this.-$f1).lambda$-com_android_server_LockSettingsService_83036((PasswordMetrics) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((LockSettingsService) this.-$f1).lambda$-com_android_server_LockSettingsService_83584(this.-$f0);
    }

    public /* synthetic */ -$Lambda$3ppOfNoHK7gU64PsFhIJAmO8LxY(int i, Object obj) {
        this.-$f0 = i;
        this.-$f1 = obj;
    }

    public final void run() {
        $m$0();
    }
}
