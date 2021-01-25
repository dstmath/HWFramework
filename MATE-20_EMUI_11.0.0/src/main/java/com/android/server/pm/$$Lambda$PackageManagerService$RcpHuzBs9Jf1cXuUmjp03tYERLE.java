package com.android.server.pm;

import java.io.File;
import java.io.FilenameFilter;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$RcpHuzBs9Jf1cXuUmjp03tYERLE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$RcpHuzBs9Jf1cXuUmjp03tYERLE implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$PackageManagerService$RcpHuzBs9Jf1cXuUmjp03tYERLE INSTANCE = new $$Lambda$PackageManagerService$RcpHuzBs9Jf1cXuUmjp03tYERLE();

    private /* synthetic */ $$Lambda$PackageManagerService$RcpHuzBs9Jf1cXuUmjp03tYERLE() {
    }

    @Override // java.io.FilenameFilter
    public final boolean accept(File file, String str) {
        return PackageManagerService.lambda$deleteTempPackageFiles$17(file, str);
    }
}
