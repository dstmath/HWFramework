package com.android.internal.telephony.dataconnection;

import android.telephony.AccessNetworkConstants;
import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.dataconnection.-$$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0 implements IntFunction {
    public static final /* synthetic */ $$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0 INSTANCE = new $$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0();

    private /* synthetic */ $$Lambda$AccessNetworksManager$QualifiedNetworks$RFnLI6POkxFwKMiSsed1qg8X7t0() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return AccessNetworkConstants.AccessNetworkType.toString(i);
    }
}
