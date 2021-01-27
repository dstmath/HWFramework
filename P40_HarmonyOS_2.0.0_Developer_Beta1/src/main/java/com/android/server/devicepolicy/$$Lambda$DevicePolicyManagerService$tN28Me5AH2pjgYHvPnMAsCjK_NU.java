package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU INSTANCE = new $$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.letters);
    }
}
