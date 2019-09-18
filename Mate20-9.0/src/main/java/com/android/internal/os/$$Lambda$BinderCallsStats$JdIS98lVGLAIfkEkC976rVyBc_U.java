package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.Comparator;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U INSTANCE = new $$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U();

    private /* synthetic */ $$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U() {
    }

    public final int compare(Object obj, Object obj2) {
        return BinderCallsStats.lambda$dump$0((BinderCallsStats.UidEntry) obj, (BinderCallsStats.UidEntry) obj2);
    }
}
