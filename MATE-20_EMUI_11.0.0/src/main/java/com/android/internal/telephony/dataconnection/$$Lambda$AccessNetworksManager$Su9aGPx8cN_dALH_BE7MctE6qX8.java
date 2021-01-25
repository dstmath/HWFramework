package com.android.internal.telephony.dataconnection;

import android.telephony.AccessNetworkConstants;
import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.dataconnection.-$$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8 implements IntFunction {
    public static final /* synthetic */ $$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8 INSTANCE = new $$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8();

    private /* synthetic */ $$Lambda$AccessNetworksManager$Su9aGPx8cN_dALH_BE7MctE6qX8() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return AccessNetworkConstants.AccessNetworkType.toString(i);
    }
}
