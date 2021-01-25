package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$CClEW-CtZQRadOocoqGh0wiKhG4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$CClEWCtZQRadOocoqGh0wiKhG4 implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$CClEWCtZQRadOocoqGh0wiKhG4 INSTANCE = new $$Lambda$DevicePolicyManagerService$CClEWCtZQRadOocoqGh0wiKhG4();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$CClEWCtZQRadOocoqGh0wiKhG4() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.symbols);
    }
}
