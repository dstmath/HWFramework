package com.android.server.wifi;

import com.android.server.wifi.NetworkSuggestionEvaluator;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$NetworkSuggestionEvaluator$MatchMetaInfo$Z-5L6mK_zEDnbf0FAnqkVI8uBr0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NetworkSuggestionEvaluator$MatchMetaInfo$Z5L6mK_zEDnbf0FAnqkVI8uBr0 implements Function {
    public static final /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$MatchMetaInfo$Z5L6mK_zEDnbf0FAnqkVI8uBr0 INSTANCE = new $$Lambda$NetworkSuggestionEvaluator$MatchMetaInfo$Z5L6mK_zEDnbf0FAnqkVI8uBr0();

    private /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$MatchMetaInfo$Z5L6mK_zEDnbf0FAnqkVI8uBr0() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((NetworkSuggestionEvaluator.PerNetworkSuggestionMatchMetaInfo) obj).matchingScanDetail.getScanResult().level);
    }
}
