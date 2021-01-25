package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJ-MQa4g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJMQa4g implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJMQa4g INSTANCE = new $$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJMQa4g();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJMQa4g() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.nonLetter);
    }
}
