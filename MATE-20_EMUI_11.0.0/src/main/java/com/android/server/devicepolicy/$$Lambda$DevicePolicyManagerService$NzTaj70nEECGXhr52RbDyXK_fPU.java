package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU INSTANCE = new $$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.length);
    }
}
