package com.android.internal.os;

import android.util.Pair;
import java.util.Comparator;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$-YP-7pwoNn8TN0iTmo5Q1r2lQz0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$YP7pwoNn8TN0iTmo5Q1r2lQz0 implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$YP7pwoNn8TN0iTmo5Q1r2lQz0 INSTANCE = new $$Lambda$BinderCallsStats$YP7pwoNn8TN0iTmo5Q1r2lQz0();

    private /* synthetic */ $$Lambda$BinderCallsStats$YP7pwoNn8TN0iTmo5Q1r2lQz0() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Integer.compare(((Pair) obj2).second.intValue(), ((Pair) obj).second.intValue());
    }
}
