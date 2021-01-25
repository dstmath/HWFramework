package com.android.server.pm;

import android.content.pm.PackageInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ApexManager$KRyGqIC_rXI5fS6Qv87QmIXpa4k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ApexManager$KRyGqIC_rXI5fS6Qv87QmIXpa4k implements Predicate {
    public static final /* synthetic */ $$Lambda$ApexManager$KRyGqIC_rXI5fS6Qv87QmIXpa4k INSTANCE = new $$Lambda$ApexManager$KRyGqIC_rXI5fS6Qv87QmIXpa4k();

    private /* synthetic */ $$Lambda$ApexManager$KRyGqIC_rXI5fS6Qv87QmIXpa4k() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ApexManager.isFactory((PackageInfo) obj);
    }
}
