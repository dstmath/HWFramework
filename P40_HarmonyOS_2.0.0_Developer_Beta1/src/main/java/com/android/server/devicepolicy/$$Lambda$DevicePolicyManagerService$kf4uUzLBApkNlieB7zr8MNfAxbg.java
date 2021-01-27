package com.android.server.devicepolicy;

import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.function.Function;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg implements Function {
    public static final /* synthetic */ $$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg INSTANCE = new $$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg();

    private /* synthetic */ $$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DevicePolicyManagerService.ActiveAdmin) obj).passwordHistoryLength);
    }
}
