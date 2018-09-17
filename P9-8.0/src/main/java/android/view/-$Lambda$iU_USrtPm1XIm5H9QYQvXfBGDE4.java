package android.view;

import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4 implements Runnable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: android.view.-$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((View) this.-$f0).-android_view_View-mthref-1();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.-$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return View.lambda$-android_view_View_420373(this.-$f0, (View) arg0);
        }

        public /* synthetic */ AnonymousClass2(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0() {
        ((View) this.-$f0).-android_view_View-mthref-0();
    }

    public /* synthetic */ -$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4(Object obj) {
        this.-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
