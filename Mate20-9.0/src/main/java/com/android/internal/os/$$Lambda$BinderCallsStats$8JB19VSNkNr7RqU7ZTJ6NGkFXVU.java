package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.Comparator;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU INSTANCE = new $$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU();

    private /* synthetic */ $$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU() {
    }

    public final int compare(Object obj, Object obj2) {
        return BinderCallsStats.lambda$dump$1((BinderCallsStats.CallStat) obj, (BinderCallsStats.CallStat) obj2);
    }
}
