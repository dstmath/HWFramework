package com.android.internal.telephony.imsphone;

import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker.IRetryTimeout;

final /* synthetic */ class -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w implements INotifyStatusChanged {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w$1 */
    final /* synthetic */ class AnonymousClass1 implements IRetryTimeout {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0() {
            return ((ImsPhoneCallTracker) this.-$f0).lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_33656();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final int get() {
            return $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((ImsPhoneCallTracker) this.-$f0).lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_32584();
    }

    public /* synthetic */ -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w(Object obj) {
        this.-$f0 = obj;
    }

    public final void notifyStatusChanged() {
        $m$0();
    }
}
