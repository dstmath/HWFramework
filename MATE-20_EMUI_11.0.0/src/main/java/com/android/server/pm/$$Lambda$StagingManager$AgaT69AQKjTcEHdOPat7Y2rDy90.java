package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$StagingManager$AgaT69AQKjTcEHdOPat7Y2rDy90  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StagingManager$AgaT69AQKjTcEHdOPat7Y2rDy90 implements Predicate {
    public static final /* synthetic */ $$Lambda$StagingManager$AgaT69AQKjTcEHdOPat7Y2rDy90 INSTANCE = new $$Lambda$StagingManager$AgaT69AQKjTcEHdOPat7Y2rDy90();

    private /* synthetic */ $$Lambda$StagingManager$AgaT69AQKjTcEHdOPat7Y2rDy90() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return StagingManager.isApexSession((PackageInstallerSession) obj);
    }
}
