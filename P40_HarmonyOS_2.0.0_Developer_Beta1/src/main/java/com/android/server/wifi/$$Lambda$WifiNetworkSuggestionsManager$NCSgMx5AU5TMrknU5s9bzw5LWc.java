package com.android.server.wifi;

import com.android.server.wifi.WifiNetworkSuggestionsManager;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bz-w5LWc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bzw5LWc implements Predicate {
    public static final /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bzw5LWc INSTANCE = new $$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bzw5LWc();

    private /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bzw5LWc() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion) obj).perAppInfo.hasUserApproved;
    }
}
