package android.print;

import android.print.PrintManager.PrintServiceRecommendationsChangeListener;
import android.print.PrintManager.PrintServiceRecommendationsChangeListenerWrapper;
import android.print.PrintManager.PrintServicesChangeListener;
import android.print.PrintManager.PrintServicesChangeListenerWrapper;

final /* synthetic */ class -$Lambda$h7xjKnKsfVuRdZMcjh_0GBiXV30 implements Runnable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: android.print.-$Lambda$h7xjKnKsfVuRdZMcjh_0GBiXV30$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            PrintServicesChangeListenerWrapper.-android_print_PrintManager$PrintServicesChangeListenerWrapper-mthref-0((PrintServicesChangeListener) this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        PrintServiceRecommendationsChangeListenerWrapper.-android_print_PrintManager$PrintServiceRecommendationsChangeListenerWrapper-mthref-0((PrintServiceRecommendationsChangeListener) this.-$f0);
    }

    public /* synthetic */ -$Lambda$h7xjKnKsfVuRdZMcjh_0GBiXV30(Object obj) {
        this.-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
