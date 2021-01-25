package com.android.server;

import com.android.internal.telephony.TelephonyPermissions;
import java.util.function.IntPredicate;

/* renamed from: com.android.server.-$$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM implements IntPredicate {
    public static final /* synthetic */ $$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM INSTANCE = new $$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM();

    private /* synthetic */ $$Lambda$TelephonyRegistry$B6olxgDfuFsbAHNXng6QnHMFZZM() {
    }

    @Override // java.util.function.IntPredicate
    public final boolean test(int i) {
        return TelephonyPermissions.checkCarrierPrivilegeForSubId(i);
    }
}
