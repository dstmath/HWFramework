package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.Comparator;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$sqXweH5BoxhmZvI188ctqYiACRk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$sqXweH5BoxhmZvI188ctqYiACRk implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$sqXweH5BoxhmZvI188ctqYiACRk INSTANCE = new $$Lambda$BinderCallsStats$sqXweH5BoxhmZvI188ctqYiACRk();

    private /* synthetic */ $$Lambda$BinderCallsStats$sqXweH5BoxhmZvI188ctqYiACRk() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return BinderCallsStats.compareByBinderClassAndCode((BinderCallsStats.ExportedCallStat) obj, (BinderCallsStats.ExportedCallStat) obj2);
    }
}
