package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$BLKFkCe_A0qn0wGueDNTdKJLGTg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$BLKFkCe_A0qn0wGueDNTdKJLGTg implements Predicate {
    public static final /* synthetic */ $$Lambda$PackageManagerService$BLKFkCe_A0qn0wGueDNTdKJLGTg INSTANCE = new $$Lambda$PackageManagerService$BLKFkCe_A0qn0wGueDNTdKJLGTg();

    private /* synthetic */ $$Lambda$PackageManagerService$BLKFkCe_A0qn0wGueDNTdKJLGTg() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return PackageManagerService.lambda$unsuspendForNonSystemSuspendingPackages$15((String) obj);
    }
}
