package com.android.server.wifi;

import com.android.server.wifi.WifiNetworkSuggestionsManager;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0-XwDSoj4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0XwDSoj4 implements Predicate {
    public static final /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0XwDSoj4 INSTANCE = new $$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0XwDSoj4();

    private /* synthetic */ $$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0XwDSoj4() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion) obj).perAppInfo.hasUserApproved;
    }
}
