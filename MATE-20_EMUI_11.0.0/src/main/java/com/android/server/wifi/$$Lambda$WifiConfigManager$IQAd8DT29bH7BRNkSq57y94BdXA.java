package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA implements Function {
    public static final /* synthetic */ $$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA INSTANCE = new $$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA();

    private /* synthetic */ $$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Long.valueOf(((WifiConfiguration) obj).lastConnected);
    }
}
