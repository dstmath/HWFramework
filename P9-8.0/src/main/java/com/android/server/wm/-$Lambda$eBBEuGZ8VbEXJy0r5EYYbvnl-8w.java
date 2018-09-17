package com.android.server.wm;

import android.content.Context;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.server.input.InputManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w implements Consumer {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((PrintWriter) this.-$f0).println((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ArrayList) this.-$f0).add((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((WindowManagerService) this.-$f0).lambda$-com_android_server_wm_WindowManagerService_154813((Runnable) this.-$f1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((WindowManagerService) this.-$f0).lambda$-com_android_server_wm_WindowManagerService_188659((IAssistScreenshotReceiver) this.-$f1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return WindowManagerService.lambda$-com_android_server_wm_WindowManagerService_215064(this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass5(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$6 */
    final /* synthetic */ class AnonymousClass6 implements Consumer {
        private final /* synthetic */ boolean -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((WindowState) arg0).lambda$-com_android_server_wm_WindowManagerService_375781(this.-$f0);
        }

        public /* synthetic */ AnonymousClass6(boolean z) {
            this.-$f0 = z;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$7 */
    final /* synthetic */ class AnonymousClass7 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            WindowManagerService.lambda$-com_android_server_wm_WindowManagerService_330593(this.-$f0, this.-$f1, (ArrayList) this.-$f2, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass7(boolean z, boolean z2, Object obj) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$8 */
    final /* synthetic */ class AnonymousClass8 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ boolean -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;

        private final /* synthetic */ void $m$0() {
            WindowManagerService.lambda$-com_android_server_wm_WindowManagerService_47633((Context) this.-$f3, (InputManagerService) this.-$f4, this.-$f0, this.-$f1, this.-$f2, (WindowManagerPolicy) this.-$f5);
        }

        public /* synthetic */ AnonymousClass8(boolean z, boolean z2, boolean z3, Object obj, Object obj2, Object obj3) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = z3;
            this.-$f3 = obj;
            this.-$f4 = obj2;
            this.-$f5 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        ((LocalService) this.-$f0).lambda$-com_android_server_wm_WindowManagerService$LocalService_365777((WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w(Object obj) {
        this.-$f0 = obj;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
