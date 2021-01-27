package com.android.internal.telephony;

import android.telephony.SubscriptionInfo;
import java.util.function.Predicate;

/* renamed from: com.android.internal.telephony.-$$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w-7n-MM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w7nMM implements Predicate {
    public static final /* synthetic */ $$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w7nMM INSTANCE = new $$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w7nMM();

    private /* synthetic */ $$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w7nMM() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return MultiSimSettingController.lambda$updatePrimarySubListAndGetChangeType$3((SubscriptionInfo) obj);
    }
}
