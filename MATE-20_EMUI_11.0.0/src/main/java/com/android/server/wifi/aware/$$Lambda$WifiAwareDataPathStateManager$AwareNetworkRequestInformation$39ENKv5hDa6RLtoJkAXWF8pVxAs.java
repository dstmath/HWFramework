package com.android.server.wifi.aware;

import android.net.NetworkRequest;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.aware.-$$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs implements Function {
    public static final /* synthetic */ $$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs INSTANCE = new $$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs();

    private /* synthetic */ $$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NetworkRequest) obj).networkCapabilities.getNetworkSpecifier();
    }
}
