package com.android.server.pm;

import android.content.pm.ProviderInfo;
import java.util.Comparator;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$2TzHnyGNanKFygnWSJ05D1sJ2Yc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$2TzHnyGNanKFygnWSJ05D1sJ2Yc implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageManagerService$2TzHnyGNanKFygnWSJ05D1sJ2Yc INSTANCE = new $$Lambda$PackageManagerService$2TzHnyGNanKFygnWSJ05D1sJ2Yc();

    private /* synthetic */ $$Lambda$PackageManagerService$2TzHnyGNanKFygnWSJ05D1sJ2Yc() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return PackageManagerService.lambda$static$10((ProviderInfo) obj, (ProviderInfo) obj2);
    }
}
