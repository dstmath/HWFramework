package com.android.server.pm;

import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$StagingManager$HJyijsQNJwcPQ10-2tU6415xlVo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StagingManager$HJyijsQNJwcPQ102tU6415xlVo implements Predicate {
    public static final /* synthetic */ $$Lambda$StagingManager$HJyijsQNJwcPQ102tU6415xlVo INSTANCE = new $$Lambda$StagingManager$HJyijsQNJwcPQ102tU6415xlVo();

    private /* synthetic */ $$Lambda$StagingManager$HJyijsQNJwcPQ102tU6415xlVo() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return StagingManager.isApexSession((PackageInstallerSession) obj);
    }
}
