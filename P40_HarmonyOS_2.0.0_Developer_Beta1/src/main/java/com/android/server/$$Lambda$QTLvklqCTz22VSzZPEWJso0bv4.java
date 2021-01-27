package com.android.server;

import android.net.NetworkScorerAppData;
import com.android.server.NetworkScoreService;
import java.util.function.Function;

/* renamed from: com.android.server.-$$Lambda$QTLvklqCTz22VSzZPEWJs-o0bv4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$QTLvklqCTz22VSzZPEWJso0bv4 implements Function {
    public static final /* synthetic */ $$Lambda$QTLvklqCTz22VSzZPEWJso0bv4 INSTANCE = new $$Lambda$QTLvklqCTz22VSzZPEWJso0bv4();

    private /* synthetic */ $$Lambda$QTLvklqCTz22VSzZPEWJso0bv4() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return new NetworkScoreService.ScoringServiceConnection((NetworkScorerAppData) obj);
    }
}
