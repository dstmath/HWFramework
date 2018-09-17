package com.android.server.wm;

import android.view.WindowManagerPolicy.ScreenOffListener;
import java.io.File;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8 implements DirectoryResolver {

    /* renamed from: com.android.server.wm.-$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((TaskSnapshotController) this.-$f0).lambda$-com_android_server_wm_TaskSnapshotController_12463((Task) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((TaskSnapshotController) this.-$f0).lambda$-com_android_server_wm_TaskSnapshotController_12298((ScreenOffListener) this.-$f1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    public final File getSystemDirectoryForUser(int i) {
        return $m$0(i);
    }
}
