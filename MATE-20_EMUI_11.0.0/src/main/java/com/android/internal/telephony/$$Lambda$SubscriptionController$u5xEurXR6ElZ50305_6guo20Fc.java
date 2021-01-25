package com.android.internal.telephony;

import java.util.Comparator;
import java.util.Map;

/* renamed from: com.android.internal.telephony.-$$Lambda$SubscriptionController$u5xE-urXR6ElZ50305_6guo20Fc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SubscriptionController$u5xEurXR6ElZ50305_6guo20Fc implements Comparator {
    public static final /* synthetic */ $$Lambda$SubscriptionController$u5xEurXR6ElZ50305_6guo20Fc INSTANCE = new $$Lambda$SubscriptionController$u5xEurXR6ElZ50305_6guo20Fc();

    private /* synthetic */ $$Lambda$SubscriptionController$u5xEurXR6ElZ50305_6guo20Fc() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return ((Integer) ((Map.Entry) obj).getKey()).compareTo((Integer) ((Map.Entry) obj2).getKey());
    }
}
