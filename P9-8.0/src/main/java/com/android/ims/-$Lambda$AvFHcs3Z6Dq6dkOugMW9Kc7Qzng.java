package com.android.ims;

import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng implements Consumer {

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$1 */
    final /* synthetic */ class AnonymousClass1 implements INotifyStatusChanged {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.-$f0).lambda$-com_android_ims_ImsManager_90082();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void notifyStatusChanged() {
            $m$0();
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((ImsConnectionStateListener) this.-$f0).equals(((ImsRegistrationListenerProxy) arg0).mListener);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.-$f1).lambda$-com_android_ims_ImsManager_43542(this.-$f0);
        }

        public /* synthetic */ AnonymousClass3(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.-$f1).lambda$-com_android_ims_ImsManager_46617(this.-$f0);
        }

        public /* synthetic */ AnonymousClass4(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
