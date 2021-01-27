package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$IMzTWqx72Nb0jxIXvoO2RbjKXNU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$IMzTWqx72Nb0jxIXvoO2RbjKXNU implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$IMzTWqx72Nb0jxIXvoO2RbjKXNU INSTANCE = new $$Lambda$PackageManagerService$IMzTWqx72Nb0jxIXvoO2RbjKXNU();

    private /* synthetic */ $$Lambda$PackageManagerService$IMzTWqx72Nb0jxIXvoO2RbjKXNU() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        SharedLibraryInfo sharedLibraryInfo = (SharedLibraryInfo) obj2;
        ((SharedLibraryInfo) obj).clearDependencies();
    }
}
