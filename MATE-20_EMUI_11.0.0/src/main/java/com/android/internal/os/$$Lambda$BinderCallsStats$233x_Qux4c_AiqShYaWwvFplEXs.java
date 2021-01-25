package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.Comparator;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$233x_Qux4c_AiqShYaWwvFplEXs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$233x_Qux4c_AiqShYaWwvFplEXs implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$233x_Qux4c_AiqShYaWwvFplEXs INSTANCE = new $$Lambda$BinderCallsStats$233x_Qux4c_AiqShYaWwvFplEXs();

    private /* synthetic */ $$Lambda$BinderCallsStats$233x_Qux4c_AiqShYaWwvFplEXs() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return BinderCallsStats.compareByCpuDesc((BinderCallsStats.ExportedCallStat) obj, (BinderCallsStats.ExportedCallStat) obj2);
    }
}
