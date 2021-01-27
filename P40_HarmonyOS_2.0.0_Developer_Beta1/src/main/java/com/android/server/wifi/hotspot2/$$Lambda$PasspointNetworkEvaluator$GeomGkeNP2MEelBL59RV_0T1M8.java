package com.android.server.wifi.hotspot2;

import com.android.server.wifi.ScanDetail;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.hotspot2.-$$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0-T1M8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0T1M8 implements Predicate {
    public static final /* synthetic */ $$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0T1M8 INSTANCE = new $$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0T1M8();

    private /* synthetic */ $$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0T1M8() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ScanDetail) obj).getNetworkDetail().isInterworking();
    }
}
