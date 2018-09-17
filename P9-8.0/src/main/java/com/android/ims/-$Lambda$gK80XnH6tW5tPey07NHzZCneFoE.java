package com.android.ims;

import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$gK80XnH6tW5tPey07NHzZCneFoE implements Consumer {

    /* renamed from: com.android.ims.-$Lambda$gK80XnH6tW5tPey07NHzZCneFoE$1 */
    final /* synthetic */ class AnonymousClass1 implements INotifyStatusChanged {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.-$f0).getStatusCallbacks().forEach(new -$Lambda$gK80XnH6tW5tPey07NHzZCneFoE());
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void notifyStatusChanged() {
            $m$0();
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
