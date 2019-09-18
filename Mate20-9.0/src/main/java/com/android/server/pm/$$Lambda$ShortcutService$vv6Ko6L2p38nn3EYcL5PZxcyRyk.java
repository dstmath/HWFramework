package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$vv6Ko6L2p38nn3EYcL5PZxcyRyk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$vv6Ko6L2p38nn3EYcL5PZxcyRyk implements Predicate {
    public static final /* synthetic */ $$Lambda$ShortcutService$vv6Ko6L2p38nn3EYcL5PZxcyRyk INSTANCE = new $$Lambda$ShortcutService$vv6Ko6L2p38nn3EYcL5PZxcyRyk();

    private /* synthetic */ $$Lambda$ShortcutService$vv6Ko6L2p38nn3EYcL5PZxcyRyk() {
    }

    public final boolean test(Object obj) {
        return ((ShortcutInfo) obj).isDynamicVisible();
    }
}
