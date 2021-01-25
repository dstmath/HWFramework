package com.android.server.wifi;

import com.android.server.wifi.WifiNetworkSuggestionsManager;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiNetworkSuggestionsManager$uA-RHVaEQ0-4Ye8cbmrdW2VQF5c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiNetworkSuggestionsManager$uARHVaEQ04Ye8cbmrdW2VQF5c implements Function {
    public static final /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$uARHVaEQ04Ye8cbmrdW2VQF5c INSTANCE = new $$Lambda$WifiNetworkSuggestionsManager$uARHVaEQ04Ye8cbmrdW2VQF5c();

    private /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$uARHVaEQ04Ye8cbmrdW2VQF5c() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((WifiNetworkSuggestionsManager.PerAppInfo) obj).maxSize);
    }
}
