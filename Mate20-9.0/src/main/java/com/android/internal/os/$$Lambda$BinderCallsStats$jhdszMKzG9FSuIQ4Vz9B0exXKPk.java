package com.android.internal.os;

import java.util.Comparator;
import java.util.Map;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk INSTANCE = new $$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk();

    private /* synthetic */ $$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Long) ((Map.Entry) obj2).getValue()).compareTo((Long) ((Map.Entry) obj).getValue());
    }
}
