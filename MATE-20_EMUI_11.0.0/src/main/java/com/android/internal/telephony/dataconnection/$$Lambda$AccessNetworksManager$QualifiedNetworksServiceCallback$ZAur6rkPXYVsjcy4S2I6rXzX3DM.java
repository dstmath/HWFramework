package com.android.internal.telephony.dataconnection;

import android.telephony.AccessNetworkConstants;
import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.dataconnection.-$$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM implements IntFunction {
    public static final /* synthetic */ $$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM INSTANCE = new $$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM();

    private /* synthetic */ $$Lambda$AccessNetworksManager$QualifiedNetworksServiceCallback$ZAur6rkPXYVsjcy4S2I6rXzX3DM() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return AccessNetworkConstants.AccessNetworkType.toString(i);
    }
}
