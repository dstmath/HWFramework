package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9-wl_G3AN15dUU8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9wl_G3AN15dUU8 implements Predicate {
    public static final /* synthetic */ $$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9wl_G3AN15dUU8 INSTANCE = new $$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9wl_G3AN15dUU8();

    private /* synthetic */ $$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9wl_G3AN15dUU8() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return PackageManagerServiceUtils.lambda$getPackagesForDexopt$6((PackageParser.Package) obj);
    }
}
