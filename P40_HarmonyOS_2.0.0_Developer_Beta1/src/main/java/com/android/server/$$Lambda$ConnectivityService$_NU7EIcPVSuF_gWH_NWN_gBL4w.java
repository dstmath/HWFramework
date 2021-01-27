package com.android.server;

import com.android.server.connectivity.NetworkAgentInfo;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.-$$Lambda$ConnectivityService$_NU7EIcPVS-uF_gWH_NWN_gBL4w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ConnectivityService$_NU7EIcPVSuF_gWH_NWN_gBL4w implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$ConnectivityService$_NU7EIcPVSuF_gWH_NWN_gBL4w INSTANCE = new $$Lambda$ConnectivityService$_NU7EIcPVSuF_gWH_NWN_gBL4w();

    private /* synthetic */ $$Lambda$ConnectivityService$_NU7EIcPVSuF_gWH_NWN_gBL4w() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((NetworkAgentInfo) obj).network.netId;
    }
}
