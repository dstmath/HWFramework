package android.view.autofill;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import java.util.List;

final /* synthetic */ class -$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            AutofillManagerClient.lambda$-android_view_autofill_AutofillManager$AutofillManagerClient_57112((AutofillManager) this.-$f0, (IntentSender) this.-$f1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f1).notifyNoFillUi(this.-$f0, (AutofillId) this.-$f2);
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

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f1).autofill(this.-$f0, (List) this.-$f2, (List) this.-$f3);
        }

        public /* synthetic */ AnonymousClass3(int i, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f2).authenticate(this.-$f0, this.-$f1, (IntentSender) this.-$f3, (Intent) this.-$f4);
        }

        public /* synthetic */ AnonymousClass4(int i, int i2, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = obj;
            this.-$f3 = obj2;
            this.-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ int -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f3).requestShowFillUi(this.-$f0, (AutofillId) this.-$f4, this.-$f1, this.-$f2, (Rect) this.-$f5, (IAutofillWindowPresenter) this.-$f6);
        }

        public /* synthetic */ AnonymousClass5(int i, int i2, int i3, Object obj, Object obj2, Object obj3, Object obj4) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = i3;
            this.-$f3 = obj;
            this.-$f4 = obj2;
            this.-$f5 = obj3;
            this.-$f6 = obj4;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$6 */
    final /* synthetic */ class AnonymousClass6 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f2).setTrackedViews(this.-$f1, (AutofillId[]) this.-$f3, this.-$f0, (AutofillId[]) this.-$f4);
        }

        public /* synthetic */ AnonymousClass6(boolean z, int i, Object obj, Object obj2, Object obj3) {
            this.-$f0 = z;
            this.-$f1 = i;
            this.-$f2 = obj;
            this.-$f3 = obj2;
            this.-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$7 */
    final /* synthetic */ class AnonymousClass7 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ boolean -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.-$f3).setState(this.-$f0, this.-$f1, this.-$f2);
        }

        public /* synthetic */ AnonymousClass7(boolean z, boolean z2, boolean z3, Object obj) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = z3;
            this.-$f3 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((AutofillManager) this.-$f0).requestHideFillUi((AutofillId) this.-$f1);
    }

    public /* synthetic */ -$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
