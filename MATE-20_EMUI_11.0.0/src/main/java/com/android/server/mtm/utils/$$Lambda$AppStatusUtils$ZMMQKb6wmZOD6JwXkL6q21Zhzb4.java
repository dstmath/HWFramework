package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4 implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4 INSTANCE = new $$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4();

    private /* synthetic */ $$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isInSmallSampleList((AwareProcessInfo) obj);
    }
}
