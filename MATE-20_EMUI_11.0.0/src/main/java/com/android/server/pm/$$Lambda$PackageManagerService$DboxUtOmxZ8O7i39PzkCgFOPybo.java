package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$DboxUtOmxZ8O7i39PzkCgFOPybo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$DboxUtOmxZ8O7i39PzkCgFOPybo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$DboxUtOmxZ8O7i39PzkCgFOPybo INSTANCE = new $$Lambda$PackageManagerService$DboxUtOmxZ8O7i39PzkCgFOPybo();

    private /* synthetic */ $$Lambda$PackageManagerService$DboxUtOmxZ8O7i39PzkCgFOPybo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((SharedLibraryInfo) obj).addDependency((SharedLibraryInfo) obj2);
    }
}
