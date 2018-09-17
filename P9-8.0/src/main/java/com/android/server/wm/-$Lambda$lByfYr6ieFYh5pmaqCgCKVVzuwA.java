package com.android.server.wm;

import com.android.internal.util.ToBooleanFunction;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$lByfYr6ieFYh5pmaqCgCKVVzuwA implements ToBooleanFunction {

    /* renamed from: com.android.server.wm.-$Lambda$lByfYr6ieFYh5pmaqCgCKVVzuwA$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((WindowManagerService) this.-$f0).-com_android_server_wm_AppWindowToken-mthref-0((WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final boolean apply(Object obj) {
        return $m$0(obj);
    }
}
