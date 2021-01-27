package com.android.server.wifi;

import com.android.server.wifi.WifiNetworkSuggestionsManager;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs implements Predicate {
    public static final /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs INSTANCE = new $$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs();

    private /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion) obj).wns.isAppInteractionRequired;
    }
}
