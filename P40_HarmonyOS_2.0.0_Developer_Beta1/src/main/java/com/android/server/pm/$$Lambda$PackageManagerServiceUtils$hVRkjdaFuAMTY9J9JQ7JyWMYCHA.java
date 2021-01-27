package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA implements Predicate {
    public static final /* synthetic */ $$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA INSTANCE = new $$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA();

    private /* synthetic */ $$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return PackageManagerServiceUtils.lambda$getPackagesForDexopt$7((PackageParser.Package) obj);
    }
}
