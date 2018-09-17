package android.accessibilityservice;

import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback;

final /* synthetic */ class -$Lambda$kpEvk0Apj34UqdMMU09LT6cNwG4 implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: android.accessibilityservice.-$Lambda$kpEvk0Apj34UqdMMU09LT6cNwG4$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((AccessibilityButtonController) this.-$f1).lambda$-android_accessibilityservice_AccessibilityButtonController_7728((AccessibilityButtonCallback) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, Object obj, Object obj2) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((AccessibilityButtonController) this.-$f0).lambda$-android_accessibilityservice_AccessibilityButtonController_6699((AccessibilityButtonCallback) this.-$f1);
    }

    public /* synthetic */ -$Lambda$kpEvk0Apj34UqdMMU09LT6cNwG4(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
