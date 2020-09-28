package com.android.ims;

import android.telephony.ims.feature.CapabilityChangeRequest;
import java.util.function.Predicate;

/* renamed from: com.android.ims.-$$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw implements Predicate {
    public static final /* synthetic */ $$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw INSTANCE = new $$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw();

    private /* synthetic */ $$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ImsManager.lambda$isImsNeeded$3((CapabilityChangeRequest.CapabilityPair) obj);
    }
}
