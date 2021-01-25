package com.android.internal.telephony;

import android.telephony.SubscriptionInfo;
import java.util.function.Function;

/* renamed from: com.android.internal.telephony.-$$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8 implements Function {
    public static final /* synthetic */ $$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8 INSTANCE = new $$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8();

    private /* synthetic */ $$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((SubscriptionInfo) obj).getSubscriptionId());
    }
}
