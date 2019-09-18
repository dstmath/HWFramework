package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$K2g8Oho05j5S7zVOkoQrHzM_Gig  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$K2g8Oho05j5S7zVOkoQrHzM_Gig implements Predicate {
    public static final /* synthetic */ $$Lambda$ShortcutService$K2g8Oho05j5S7zVOkoQrHzM_Gig INSTANCE = new $$Lambda$ShortcutService$K2g8Oho05j5S7zVOkoQrHzM_Gig();

    private /* synthetic */ $$Lambda$ShortcutService$K2g8Oho05j5S7zVOkoQrHzM_Gig() {
    }

    public final boolean test(Object obj) {
        return ((ShortcutInfo) obj).isPinnedVisible();
    }
}
