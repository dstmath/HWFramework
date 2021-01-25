package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag INSTANCE = new $$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).minimumPasswordMetrics.upperCase);
    }
}
