package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$StagingManager$j1RpPmMrsxcldNpyt2n2wcJbVA0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StagingManager$j1RpPmMrsxcldNpyt2n2wcJbVA0 implements Predicate {
    public static final /* synthetic */ $$Lambda$StagingManager$j1RpPmMrsxcldNpyt2n2wcJbVA0 INSTANCE = new $$Lambda$StagingManager$j1RpPmMrsxcldNpyt2n2wcJbVA0();

    private /* synthetic */ $$Lambda$StagingManager$j1RpPmMrsxcldNpyt2n2wcJbVA0() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return StagingManager.lambda$sessionContainsApk$6((PackageInstallerSession) obj);
    }
}
