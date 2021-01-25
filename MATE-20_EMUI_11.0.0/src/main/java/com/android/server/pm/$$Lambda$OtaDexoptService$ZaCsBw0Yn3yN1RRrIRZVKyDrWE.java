package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: com.android.server.pm.-$$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZV-KyDrWE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZVKyDrWE implements Comparator {
    public static final /* synthetic */ $$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZVKyDrWE INSTANCE = new $$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZVKyDrWE();

    private /* synthetic */ $$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZVKyDrWE() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Long.compare(((PackageParser.Package) obj).getLatestForegroundPackageUseTimeInMills(), ((PackageParser.Package) obj2).getLatestForegroundPackageUseTimeInMills());
    }
}
