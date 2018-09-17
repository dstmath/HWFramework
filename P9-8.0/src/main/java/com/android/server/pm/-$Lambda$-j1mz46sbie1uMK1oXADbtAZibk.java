package com.android.server.pm;

import java.util.Comparator;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$-j1mz46sbie1uMK1oXADbtAZibk implements Comparator {

    /* renamed from: com.android.server.pm.-$Lambda$-j1mz46sbie1uMK1oXADbtAZibk$5 */
    final /* synthetic */ class AnonymousClass5 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutPackage) this.-$f0).lambda$-com_android_server_pm_ShortcutPackage_14671((ShortcutLauncher) arg0);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
