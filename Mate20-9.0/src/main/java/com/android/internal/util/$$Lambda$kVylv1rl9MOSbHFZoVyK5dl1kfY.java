package com.android.internal.util;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.internal.util.-$$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY implements Predicate {
    public static final /* synthetic */ $$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY INSTANCE = new $$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY();

    private /* synthetic */ $$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY() {
    }

    public final boolean test(Object obj) {
        return DumpUtils.isPlatformPackage((ComponentName.WithComponentName) obj);
    }
}
