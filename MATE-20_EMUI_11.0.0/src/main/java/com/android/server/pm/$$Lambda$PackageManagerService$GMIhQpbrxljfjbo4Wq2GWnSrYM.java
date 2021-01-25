package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$GMIhQpbrxlj-fjbo4Wq2GWnSrYM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$GMIhQpbrxljfjbo4Wq2GWnSrYM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$GMIhQpbrxljfjbo4Wq2GWnSrYM INSTANCE = new $$Lambda$PackageManagerService$GMIhQpbrxljfjbo4Wq2GWnSrYM();

    private /* synthetic */ $$Lambda$PackageManagerService$GMIhQpbrxljfjbo4Wq2GWnSrYM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        SharedLibraryInfo sharedLibraryInfo = (SharedLibraryInfo) obj2;
        ((SharedLibraryInfo) obj).clearDependencies();
    }
}
