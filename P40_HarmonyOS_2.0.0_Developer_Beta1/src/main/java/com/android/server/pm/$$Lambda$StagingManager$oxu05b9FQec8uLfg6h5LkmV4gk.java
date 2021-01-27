package com.android.server.pm;

import java.util.function.ToIntFunction;

/* renamed from: com.android.server.pm.-$$Lambda$StagingManager$ox-u05b9FQec8uLfg6h5LkmV4gk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StagingManager$oxu05b9FQec8uLfg6h5LkmV4gk implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$StagingManager$oxu05b9FQec8uLfg6h5LkmV4gk INSTANCE = new $$Lambda$StagingManager$oxu05b9FQec8uLfg6h5LkmV4gk();

    private /* synthetic */ $$Lambda$StagingManager$oxu05b9FQec8uLfg6h5LkmV4gk() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((PackageInstallerSession) obj).sessionId;
    }
}
