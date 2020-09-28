package com.android.internal.util;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.internal.util.-$$Lambda$grRTg3idX3yJe9Zyx-tmLBiD1DM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$grRTg3idX3yJe9ZyxtmLBiD1DM implements Predicate {
    public static final /* synthetic */ $$Lambda$grRTg3idX3yJe9ZyxtmLBiD1DM INSTANCE = new $$Lambda$grRTg3idX3yJe9ZyxtmLBiD1DM();

    private /* synthetic */ $$Lambda$grRTg3idX3yJe9ZyxtmLBiD1DM() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return DumpUtils.isPlatformCriticalPackage((ComponentName.WithComponentName) obj);
    }
}
