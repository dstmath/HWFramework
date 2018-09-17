package com.android.server.wm;

import android.util.SparseIntArray;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0 implements Consumer {

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((RootWindowContainer) this.-$f0).lambda$-com_android_server_wm_RootWindowContainer_8205((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((RootWindowContainer) this.-$f0).lambda$-com_android_server_wm_RootWindowContainer_21850((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((RootWindowContainer) this.-$f0).lambda$-com_android_server_wm_RootWindowContainer_25154((SparseIntArray) this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$4 */
    final /* synthetic */ class AnonymousClass4 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return RootWindowContainer.lambda$-com_android_server_wm_RootWindowContainer_22491(this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass4(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$5 */
    final /* synthetic */ class AnonymousClass5 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            RootWindowContainer.lambda$-com_android_server_wm_RootWindowContainer_16716((String) this.-$f1, (ArrayList) this.-$f2, this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass5(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$6 */
    final /* synthetic */ class AnonymousClass6 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ void $m$0(Object arg0) {
            RootWindowContainer.lambda$-com_android_server_wm_RootWindowContainer_52695((ArrayList) this.-$f1, (PrintWriter) this.-$f2, (int[]) this.-$f3, this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass6(boolean z, Object obj, Object obj2, Object obj3) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$7 */
    final /* synthetic */ class AnonymousClass7 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            RootWindowContainer.lambda$-com_android_server_wm_RootWindowContainer_21585(this.-$f1, this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass7(boolean z, int i) {
            this.-$f0 = z;
            this.-$f1 = i;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
