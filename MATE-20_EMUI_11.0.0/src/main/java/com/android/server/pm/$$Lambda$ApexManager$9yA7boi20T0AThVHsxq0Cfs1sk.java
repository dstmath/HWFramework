package com.android.server.pm;

import android.content.pm.PackageInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ApexManager$9yA7boi20-T0AThVHsxq0Cfs1sk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ApexManager$9yA7boi20T0AThVHsxq0Cfs1sk implements Predicate {
    public static final /* synthetic */ $$Lambda$ApexManager$9yA7boi20T0AThVHsxq0Cfs1sk INSTANCE = new $$Lambda$ApexManager$9yA7boi20T0AThVHsxq0Cfs1sk();

    private /* synthetic */ $$Lambda$ApexManager$9yA7boi20T0AThVHsxq0Cfs1sk() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ApexManager.isActive((PackageInfo) obj);
    }
}
