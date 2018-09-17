package com.android.server.wm;

import android.view.Choreographer.FrameCallback;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY implements Consumer {

    /* renamed from: com.android.server.wm.-$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY$1 */
    final /* synthetic */ class AnonymousClass1 implements FrameCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((WindowAnimator) this.-$f0).lambda$-com_android_server_wm_WindowAnimator_4498(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void doFrame(long j) {
            $m$0(j);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((WindowAnimator) this.-$f0).lambda$-com_android_server_wm_WindowAnimator_4391();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
