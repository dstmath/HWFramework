package android.telecom;

import android.telecom.Call.Callback;
import android.telecom.Call.RttCall;

final /* synthetic */ class -$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo implements Runnable {
    private final /* synthetic */ int -$f0;
    private final /* synthetic */ Object -$f1;
    private final /* synthetic */ Object -$f2;

    /* renamed from: android.telecom.-$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((Callback) this.-$f1).lambda$-android_telecom_Call_71379((Call) this.-$f2, this.-$f0);
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

    /* renamed from: android.telecom.-$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((Callback) this.-$f1).lambda$-android_telecom_Call_71038((Call) this.-$f2, this.-$f0);
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

    /* renamed from: android.telecom.-$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ void $m$0() {
            ((Callback) this.-$f1).lambda$-android_telecom_Call_78088((Call) this.-$f2, this.-$f0, (RttCall) this.-$f3);
        }

        public /* synthetic */ AnonymousClass3(boolean z, Object obj, Object obj2, Object obj3) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((Callback) this.-$f1).lambda$-android_telecom_Call_78527((Call) this.-$f2, this.-$f0);
    }

    public /* synthetic */ -$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo(int i, Object obj, Object obj2) {
        this.-$f0 = i;
        this.-$f1 = obj;
        this.-$f2 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
