package com.android.internal.telephony;

import android.telephony.SubscriptionInfo;
import java.util.function.Function;

/* renamed from: com.android.internal.telephony.-$$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg implements Function {
    public static final /* synthetic */ $$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg INSTANCE = new $$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg();

    private /* synthetic */ $$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return NetworkScanRequestTracker.getAllowableMccMncsFromSubscriptionInfo((SubscriptionInfo) obj);
    }
}
