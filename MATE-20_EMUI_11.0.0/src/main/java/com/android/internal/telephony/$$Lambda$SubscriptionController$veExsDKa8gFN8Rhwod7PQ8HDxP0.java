package com.android.internal.telephony;

import android.telephony.SubscriptionInfo;
import java.util.function.ToIntFunction;

/* renamed from: com.android.internal.telephony.-$$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0 implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0 INSTANCE = new $$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0();

    private /* synthetic */ $$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((SubscriptionInfo) obj).getSubscriptionId();
    }
}
