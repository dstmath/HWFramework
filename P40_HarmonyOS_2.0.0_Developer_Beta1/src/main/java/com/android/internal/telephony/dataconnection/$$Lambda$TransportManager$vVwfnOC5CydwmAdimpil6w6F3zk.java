package com.android.internal.telephony.dataconnection;

import android.telephony.AccessNetworkConstants;
import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.dataconnection.-$$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk implements IntFunction {
    public static final /* synthetic */ $$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk INSTANCE = new $$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk();

    private /* synthetic */ $$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return AccessNetworkConstants.transportTypeToString(i);
    }
}
