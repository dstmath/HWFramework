package com.android.internal.util;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.internal.util.-$$Lambda$TCbPpgWlKJUHZgFKCczglAvxLfw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TCbPpgWlKJUHZgFKCczglAvxLfw implements Predicate {
    public static final /* synthetic */ $$Lambda$TCbPpgWlKJUHZgFKCczglAvxLfw INSTANCE = new $$Lambda$TCbPpgWlKJUHZgFKCczglAvxLfw();

    private /* synthetic */ $$Lambda$TCbPpgWlKJUHZgFKCczglAvxLfw() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return DumpUtils.isPlatformNonCriticalPackage((ComponentName.WithComponentName) obj);
    }
}
