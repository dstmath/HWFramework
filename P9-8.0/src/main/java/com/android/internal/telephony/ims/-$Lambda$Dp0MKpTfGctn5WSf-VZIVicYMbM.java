package com.android.internal.telephony.ims;

import android.util.Pair;
import com.android.internal.telephony.ims.ImsServiceController.RebindRetry;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$Dp0MKpTfGctn5WSf-VZIVicYMbM implements RebindRetry {

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$Dp0MKpTfGctn5WSf-VZIVicYMbM$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ImsServiceController.lambda$-com_android_internal_telephony_ims_ImsServiceController_19011((Pair) this.-$f0, (ImsFeatureStatusCallback) arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    public final long getRetryTimeout() {
        return $m$0();
    }
}
