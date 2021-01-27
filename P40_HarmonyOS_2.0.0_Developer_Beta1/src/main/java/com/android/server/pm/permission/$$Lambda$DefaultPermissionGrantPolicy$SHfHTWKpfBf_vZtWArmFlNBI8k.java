package com.android.server.pm.permission;

import java.util.function.IntFunction;

/* renamed from: com.android.server.pm.permission.-$$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArm-FlNBI8k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArmFlNBI8k implements IntFunction {
    public static final /* synthetic */ $$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArmFlNBI8k INSTANCE = new $$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArmFlNBI8k();

    private /* synthetic */ $$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArmFlNBI8k() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return DefaultPermissionGrantPolicy.lambda$grantRuntimePermissions$0(i);
    }
}
