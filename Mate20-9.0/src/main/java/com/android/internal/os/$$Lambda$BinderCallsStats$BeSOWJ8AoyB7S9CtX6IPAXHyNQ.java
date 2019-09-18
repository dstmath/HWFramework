package com.android.internal.os;

import java.util.Comparator;
import java.util.Map;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX-6IPAXHyNQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX6IPAXHyNQ implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX6IPAXHyNQ INSTANCE = new $$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX6IPAXHyNQ();

    private /* synthetic */ $$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX6IPAXHyNQ() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Long) ((Map.Entry) obj2).getValue()).compareTo((Long) ((Map.Entry) obj).getValue());
    }
}
