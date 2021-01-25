package com.android.server.policy;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.policy.-$$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw implements TriConsumer {
    public static final /* synthetic */ $$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw INSTANCE = new $$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw();

    private /* synthetic */ $$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PermissionPolicyService) obj).synchronizePackagePermissionsAndAppOpsForUser((String) obj2, ((Integer) obj3).intValue());
    }
}
