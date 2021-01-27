package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8 implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8 INSTANCE = new $$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.lowerCase);
    }
}
