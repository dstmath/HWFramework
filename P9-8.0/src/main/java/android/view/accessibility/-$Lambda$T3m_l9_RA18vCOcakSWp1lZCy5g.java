package android.view.accessibility;

import android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;

final /* synthetic */ class -$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: android.view.accessibility.-$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((AccessibilityStateChangeListener) this.-$f1).lambda$-android_view_accessibility_AccessibilityManager_36305(this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, Object obj) {
            this.-$f0 = z;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.accessibility.-$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((HighTextContrastChangeListener) this.-$f1).lambda$-android_view_accessibility_AccessibilityManager_38227(this.-$f0);
        }

        public /* synthetic */ AnonymousClass2(boolean z, Object obj) {
            this.-$f0 = z;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.accessibility.-$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((TouchExplorationStateChangeListener) this.-$f1).lambda$-android_view_accessibility_AccessibilityManager_37264(this.-$f0);
        }

        public /* synthetic */ AnonymousClass3(boolean z, Object obj) {
            this.-$f0 = z;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((android.view.accessibility.AccessibilityManager.AnonymousClass1) this.-$f0).lambda$-android_view_accessibility_AccessibilityManager$1_8646((AccessibilityServicesStateChangeListener) this.-$f1);
    }

    public /* synthetic */ -$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
