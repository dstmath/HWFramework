package com.android.server.pm;

import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ implements Consumer {

    /* renamed from: com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutUser) this.-$f0).lambda$-com_android_server_pm_ShortcutUser_18230((ShortcutService) this.-$f1, (ShortcutLauncher) arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutUser) this.-$f0).lambda$-com_android_server_pm_ShortcutUser_18617((ShortcutService) this.-$f1, (ShortcutPackage) arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ$4 */
    final /* synthetic */ class AnonymousClass4 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            ShortcutUser.lambda$-com_android_server_pm_ShortcutUser_8403(this.-$f0, (String) this.-$f1, (Consumer) this.-$f2, (ShortcutPackageItem) arg0);
        }

        public /* synthetic */ AnonymousClass4(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
