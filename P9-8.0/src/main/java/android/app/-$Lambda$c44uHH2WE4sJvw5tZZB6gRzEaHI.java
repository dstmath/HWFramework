package android.app;

import android.view.Window.OnWindowSwipeDismissedCallback;

final /* synthetic */ class -$Lambda$c44uHH2WE4sJvw5tZZB6gRzEaHI implements OnWindowSwipeDismissedCallback {
    private final /* synthetic */ Object -$f0;

    /* renamed from: android.app.-$Lambda$c44uHH2WE4sJvw5tZZB6gRzEaHI$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((Dialog) this.-$f0).-android_app_Dialog-mthref-0();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((Dialog) this.-$f0).lambda$-android_app_Dialog_10533();
    }

    public /* synthetic */ -$Lambda$c44uHH2WE4sJvw5tZZB6gRzEaHI(Object obj) {
        this.-$f0 = obj;
    }

    public final void onWindowSwipeDismissed() {
        $m$0();
    }
}
