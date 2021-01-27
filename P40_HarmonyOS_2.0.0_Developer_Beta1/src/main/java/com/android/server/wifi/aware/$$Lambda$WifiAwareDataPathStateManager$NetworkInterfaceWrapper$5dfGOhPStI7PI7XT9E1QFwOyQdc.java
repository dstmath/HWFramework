package com.android.server.wifi.aware;

import android.net.NetworkRequest;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.aware.-$$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc implements Function {
    public static final /* synthetic */ $$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc INSTANCE = new $$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc();

    private /* synthetic */ $$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NetworkRequest) obj).networkCapabilities.getNetworkSpecifier();
    }
}
