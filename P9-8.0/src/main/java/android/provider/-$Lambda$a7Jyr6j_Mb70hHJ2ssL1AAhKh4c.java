package android.provider;

import android.content.Context;
import android.graphics.Typeface;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.FontsContract.FontRequestCallback;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

final /* synthetic */ class -$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c implements Comparator {

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$10 */
    final /* synthetic */ class AnonymousClass10 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_24540((Typeface) this.-$f1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$11 */
    final /* synthetic */ class AnonymousClass11 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0() {
            FontsContract.lambda$-android_provider_FontsContract_20996((Context) this.-$f0, (CancellationSignal) this.-$f1, (FontRequest) this.-$f2, (Handler) this.-$f3, (FontRequestCallback) this.-$f4);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$12 */
    final /* synthetic */ class AnonymousClass12 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;

        private final /* synthetic */ void $m$0() {
            FontsContract.lambda$-android_provider_FontsContract_13824((FontRequest) this.-$f0, (String) this.-$f1, (AtomicReference) this.-$f2, (Lock) this.-$f3, (AtomicBoolean) this.-$f4, (AtomicBoolean) this.-$f5, (Condition) this.-$f6);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
            this.-$f5 = obj6;
            this.-$f6 = obj7;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$13 */
    final /* synthetic */ class AnonymousClass13 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f1).lambda$-android_provider_FontsContract_23796(this.-$f0);
        }

        public /* synthetic */ AnonymousClass13(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-2);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-3);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-3);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$6 */
    final /* synthetic */ class AnonymousClass6 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-3);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$7 */
    final /* synthetic */ class AnonymousClass7 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_23796(-3);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$8 */
    final /* synthetic */ class AnonymousClass8 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_24540((Typeface) this.-$f1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c$9 */
    final /* synthetic */ class AnonymousClass9 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((FontRequestCallback) this.-$f0).lambda$-android_provider_FontsContract_24540((Typeface) this.-$f1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
