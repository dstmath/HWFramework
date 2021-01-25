package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc INSTANCE = new $$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc();

    private /* synthetic */ $$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Long.compare(((PackageParser.Package) obj).getLatestForegroundPackageUseTimeInMills(), ((PackageParser.Package) obj2).getLatestForegroundPackageUseTimeInMills());
    }
}
