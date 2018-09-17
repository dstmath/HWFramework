package com.android.server.pm;

import java.util.concurrent.CountDownLatch;

final /* synthetic */ class -$Lambda$hS1mIPNPrUgj3Ey9GdylMJh-bQA implements Runnable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.pm.-$Lambda$hS1mIPNPrUgj3Ey9GdylMJh-bQA$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((CountDownLatch) this.-$f0).countDown();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((ShortcutBitmapSaver) this.-$f0).lambda$-com_android_server_pm_ShortcutBitmapSaver_7645();
    }

    public /* synthetic */ -$Lambda$hS1mIPNPrUgj3Ey9GdylMJh-bQA(Object obj) {
        this.-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
