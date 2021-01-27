package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo INSTANCE = new $$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo();

    private /* synthetic */ $$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isCameraRecord((AwareProcessInfo) obj);
    }
}
