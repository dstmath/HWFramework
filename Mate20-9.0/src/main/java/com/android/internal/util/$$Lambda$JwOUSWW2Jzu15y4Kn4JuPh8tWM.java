package com.android.internal.util;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.internal.util.-$$Lambda$JwOUSWW2-Jzu15y4Kn4JuPh8tWM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$JwOUSWW2Jzu15y4Kn4JuPh8tWM implements Predicate {
    public static final /* synthetic */ $$Lambda$JwOUSWW2Jzu15y4Kn4JuPh8tWM INSTANCE = new $$Lambda$JwOUSWW2Jzu15y4Kn4JuPh8tWM();

    private /* synthetic */ $$Lambda$JwOUSWW2Jzu15y4Kn4JuPh8tWM() {
    }

    public final boolean test(Object obj) {
        return DumpUtils.isNonPlatformPackage((ComponentName.WithComponentName) obj);
    }
}
