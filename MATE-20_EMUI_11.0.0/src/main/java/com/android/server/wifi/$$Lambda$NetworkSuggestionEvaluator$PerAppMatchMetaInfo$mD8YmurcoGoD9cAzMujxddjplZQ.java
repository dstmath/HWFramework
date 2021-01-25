package com.android.server.wifi;

import com.android.server.wifi.NetworkSuggestionEvaluator;
import java.util.List;
import java.util.function.BinaryOperator;

/* renamed from: com.android.server.wifi.-$$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$mD8YmurcoGoD9cAzMujxddjplZQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$mD8YmurcoGoD9cAzMujxddjplZQ implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$mD8YmurcoGoD9cAzMujxddjplZQ INSTANCE = new $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$mD8YmurcoGoD9cAzMujxddjplZQ();

    private /* synthetic */ $$Lambda$NetworkSuggestionEvaluator$PerAppMatchMetaInfo$mD8YmurcoGoD9cAzMujxddjplZQ() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return NetworkSuggestionEvaluator.PerAppMatchMetaInfo.lambda$getHighestPriorityNetworks$2((List) obj, (List) obj2);
    }
}
