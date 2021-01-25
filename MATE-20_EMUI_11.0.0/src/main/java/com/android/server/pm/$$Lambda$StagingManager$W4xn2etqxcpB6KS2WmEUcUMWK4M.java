package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$StagingManager$W4xn2etqxcpB6KS2WmEUcUMWK4M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StagingManager$W4xn2etqxcpB6KS2WmEUcUMWK4M implements Predicate {
    public static final /* synthetic */ $$Lambda$StagingManager$W4xn2etqxcpB6KS2WmEUcUMWK4M INSTANCE = new $$Lambda$StagingManager$W4xn2etqxcpB6KS2WmEUcUMWK4M();

    private /* synthetic */ $$Lambda$StagingManager$W4xn2etqxcpB6KS2WmEUcUMWK4M() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return StagingManager.lambda$installApksInSession$9((PackageInstallerSession) obj);
    }
}
