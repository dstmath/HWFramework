package com.android.server.wifi;

import com.android.server.wifi.NetworkSuggestionEvaluator;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$4zBwRelAVwggSH4KkLdQq5J6uMs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$4zBwRelAVwggSH4KkLdQq5J6uMs implements Function {
    public static final /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$4zBwRelAVwggSH4KkLdQq5J6uMs INSTANCE = new $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$4zBwRelAVwggSH4KkLdQq5J6uMs();

    private /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$4zBwRelAVwggSH4KkLdQq5J6uMs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((NetworkSuggestionEvaluator.PerNetworkSuggestionMatchMetaInfo) obj).wifiNetworkSuggestion.wifiConfiguration.priority);
    }
}
