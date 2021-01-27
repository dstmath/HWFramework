package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs INSTANCE = new $$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs();

    private /* synthetic */ $$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isAchScreenChangedNum((AwareProcessInfo) obj);
    }
}
