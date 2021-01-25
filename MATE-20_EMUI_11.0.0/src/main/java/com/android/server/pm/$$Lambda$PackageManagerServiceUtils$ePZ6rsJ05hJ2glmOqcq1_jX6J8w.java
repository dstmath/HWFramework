package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w INSTANCE = new $$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w();

    private /* synthetic */ $$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Long.compare(((PackageParser.Package) obj2).getLatestForegroundPackageUseTimeInMills(), ((PackageParser.Package) obj).getLatestForegroundPackageUseTimeInMills());
    }
}
