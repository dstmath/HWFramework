package com.android.server.wm;

import android.graphics.Rect;
import android.os.IBinder;
import android.util.MutableBoolean;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.WindowState;
import com.android.internal.util.ToBooleanFunction;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI implements Screenshoter {

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$10 */
    final /* synthetic */ class AnonymousClass10 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_21155((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass10(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$11 */
    final /* synthetic */ class AnonymousClass11 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_23104((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass11(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$12 */
    final /* synthetic */ class AnonymousClass12 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_26653((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass12(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$13 */
    final /* synthetic */ class AnonymousClass13 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_27073((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass13(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$14 */
    final /* synthetic */ class AnonymousClass14 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_32034((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass14(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$15 */
    final /* synthetic */ class AnonymousClass15 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_34058((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$16 */
    final /* synthetic */ class AnonymousClass16 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_34263((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass16(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$17 */
    final /* synthetic */ class AnonymousClass17 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_117385((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass17(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$18 */
    final /* synthetic */ class AnonymousClass18 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((WindowState) arg0).mWinAnimator.enableSurfaceTrace((FileDescriptor) this.-$f0);
        }

        public /* synthetic */ AnonymousClass18(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$19 */
    final /* synthetic */ class AnonymousClass19 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_161672((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass19(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$20 */
    final /* synthetic */ class AnonymousClass20 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_135678((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass20(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$21 */
    final /* synthetic */ class AnonymousClass21 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_33773((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass21(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$22 */
    final /* synthetic */ class AnonymousClass22 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_129718((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass22(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$23 */
    final /* synthetic */ class AnonymousClass23 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_136443((WindowManagerPolicy) this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass23(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$24 */
    final /* synthetic */ class AnonymousClass24 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_137112((WindowManagerPolicy) this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass24(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$25 */
    final /* synthetic */ class AnonymousClass25 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            DisplayContent.lambda$-com_android_server_wm_DisplayContent_127971((PrintWriter) this.-$f0, (String) this.-$f1, (int[]) this.-$f2, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass25(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$26 */
    final /* synthetic */ class AnonymousClass26 implements Predicate {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_126204((WindowState) this.-$f1, (WindowState) this.-$f2, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass26(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$27 */
    final /* synthetic */ class AnonymousClass27 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return DisplayContent.lambda$-com_android_server_wm_DisplayContent_114587(this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass27(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$28 */
    final /* synthetic */ class AnonymousClass28 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return DisplayContent.lambda$-com_android_server_wm_DisplayContent_114766(this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass28(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$29 */
    final /* synthetic */ class AnonymousClass29 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((WindowState) arg0).mWinAnimator.seamlesslyRotateWindow(this.-$f0, this.-$f1);
        }

        public /* synthetic */ AnonymousClass29(int i, int i2) {
            this.-$f0 = i;
            this.-$f1 = i2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$30 */
    final /* synthetic */ class AnonymousClass30 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            DisplayContent.lambda$-com_android_server_wm_DisplayContent_134746(this.-$f0, this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass30(int i, int i2) {
            this.-$f0 = i;
            this.-$f1 = i2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$31 */
    final /* synthetic */ class AnonymousClass31 implements Predicate {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f2).lambda$-com_android_server_wm_DisplayContent_113683(this.-$f0, this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass31(int i, int i2, Object obj) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$32 */
    final /* synthetic */ class AnonymousClass32 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f1).lambda$-com_android_server_wm_DisplayContent_57485(this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass32(boolean z, Object obj) {
            this.-$f0 = z;
            this.-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$33 */
    final /* synthetic */ class AnonymousClass33 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            DisplayContent.lambda$-com_android_server_wm_DisplayContent_129043((WindowManagerPolicy) this.-$f2, this.-$f0, this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass33(boolean z, boolean z2, Object obj) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$34 */
    final /* synthetic */ class AnonymousClass34 implements ToBooleanFunction {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ int -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f3).lambda$-com_android_server_wm_DisplayContent_148533(this.-$f2, this.-$f0, (IBinder) this.-$f4, (MutableBoolean) this.-$f5, this.-$f1, (Rect) this.-$f6, (Rect) this.-$f7, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass34(boolean z, boolean z2, int i, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = i;
            this.-$f3 = obj;
            this.-$f4 = obj2;
            this.-$f5 = obj3;
            this.-$f6 = obj4;
            this.-$f7 = obj5;
        }

        public final boolean apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$7 */
    final /* synthetic */ class AnonymousClass7 implements ToBooleanFunction {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_23600((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$8 */
    final /* synthetic */ class AnonymousClass8 implements Comparator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((NonAppWindowContainers) this.-$f0).lambda$-com_android_server_wm_DisplayContent$NonAppWindowContainers_175004((WindowToken) arg0, (WindowToken) arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$9 */
    final /* synthetic */ class AnonymousClass9 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.-$f0).lambda$-com_android_server_wm_DisplayContent_18842((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass9(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final Object screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) {
        return $m$0(rect, i, i2, i3, i4, z, i5);
    }
}
