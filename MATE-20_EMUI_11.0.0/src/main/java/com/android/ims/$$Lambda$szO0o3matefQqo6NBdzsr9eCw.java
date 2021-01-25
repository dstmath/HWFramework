package com.android.ims;

import android.telephony.SubscriptionInfo;
import java.util.function.Function;

/* renamed from: com.android.ims.-$$Lambda$szO0o3matefQqo-6NB-dzsr9eCw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$szO0o3matefQqo6NBdzsr9eCw implements Function {
    public static final /* synthetic */ $$Lambda$szO0o3matefQqo6NBdzsr9eCw INSTANCE = new $$Lambda$szO0o3matefQqo6NBdzsr9eCw();

    private /* synthetic */ $$Lambda$szO0o3matefQqo6NBdzsr9eCw() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((SubscriptionInfo) obj).getSubscriptionId());
    }
}
