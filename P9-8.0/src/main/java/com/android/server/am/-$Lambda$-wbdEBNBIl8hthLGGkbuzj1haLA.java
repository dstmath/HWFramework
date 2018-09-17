package com.android.server.am;

import com.android.server.am.UserController.AnonymousClass3;

final /* synthetic */ class -$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: com.android.server.am.-$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((AnonymousClass3) this.-$f2).lambda$-com_android_server_am_UserController$3_23161(this.-$f1, this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, int i, Object obj) {
            this.-$f0 = z;
            this.-$f1 = i;
            this.-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((UserController) this.-$f0).lambda$-com_android_server_am_UserController_19227((UserState) this.-$f1);
    }

    public /* synthetic */ -$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
