package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$vv6Ko6L2p38nn3EYcL5PZxcyRyk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$vv6Ko6L2p38nn3EYcL5PZxcyRyk implements Predicate {
    public static final /* synthetic */ $$Lambda$vv6Ko6L2p38nn3EYcL5PZxcyRyk INSTANCE = new $$Lambda$vv6Ko6L2p38nn3EYcL5PZxcyRyk();

    private /* synthetic */ $$Lambda$vv6Ko6L2p38nn3EYcL5PZxcyRyk() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ShortcutInfo) obj).isDynamicVisible();
    }
}
