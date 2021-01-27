package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$0uSmqyontvENCPVNP09rputt8co  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$0uSmqyontvENCPVNP09rputt8co implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$0uSmqyontvENCPVNP09rputt8co INSTANCE = new $$Lambda$PackageManagerService$0uSmqyontvENCPVNP09rputt8co();

    private /* synthetic */ $$Lambda$PackageManagerService$0uSmqyontvENCPVNP09rputt8co() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((SharedLibraryInfo) obj).addDependency((SharedLibraryInfo) obj2);
    }
}
