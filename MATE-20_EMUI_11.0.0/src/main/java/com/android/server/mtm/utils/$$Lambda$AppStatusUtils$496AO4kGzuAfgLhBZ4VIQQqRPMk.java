package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk INSTANCE = new $$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk();

    private /* synthetic */ $$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isScreenRecord((AwareProcessInfo) obj);
    }
}
