package com.android.server.pm;

import android.content.pm.PackageParser;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerServiceUtils$QMV-UHbRIK26QMZL5iM27MchX7U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerServiceUtils$QMVUHbRIK26QMZL5iM27MchX7U implements Predicate {
    public static final /* synthetic */ $$Lambda$PackageManagerServiceUtils$QMVUHbRIK26QMZL5iM27MchX7U INSTANCE = new $$Lambda$PackageManagerServiceUtils$QMVUHbRIK26QMZL5iM27MchX7U();

    private /* synthetic */ $$Lambda$PackageManagerServiceUtils$QMVUHbRIK26QMZL5iM27MchX7U() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((PackageParser.Package) obj).coreApp;
    }
}
