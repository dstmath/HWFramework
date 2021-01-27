package com.android.server.pm;

import java.io.File;
import java.io.FilenameFilter;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$62xvSFYLew0DW-oz4SkFLSkHjBU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$62xvSFYLew0DWoz4SkFLSkHjBU implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$PackageManagerService$62xvSFYLew0DWoz4SkFLSkHjBU INSTANCE = new $$Lambda$PackageManagerService$62xvSFYLew0DWoz4SkFLSkHjBU();

    private /* synthetic */ $$Lambda$PackageManagerService$62xvSFYLew0DWoz4SkFLSkHjBU() {
    }

    @Override // java.io.FilenameFilter
    public final boolean accept(File file, String str) {
        return PackageManagerService.lambda$deleteTempPackageFiles$19(file, str);
    }
}
