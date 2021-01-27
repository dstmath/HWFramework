package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6tr-DFfrGE5TE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6trDFfrGE5TE implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6trDFfrGE5TE INSTANCE = new $$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6trDFfrGE5TE();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6trDFfrGE5TE() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.numeric);
    }
}
