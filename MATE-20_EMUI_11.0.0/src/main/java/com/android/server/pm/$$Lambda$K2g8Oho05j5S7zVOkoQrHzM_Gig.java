package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$K2g8Oho05j5S7zVOkoQrHzM_Gig  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$K2g8Oho05j5S7zVOkoQrHzM_Gig implements Predicate {
    public static final /* synthetic */ $$Lambda$K2g8Oho05j5S7zVOkoQrHzM_Gig INSTANCE = new $$Lambda$K2g8Oho05j5S7zVOkoQrHzM_Gig();

    private /* synthetic */ $$Lambda$K2g8Oho05j5S7zVOkoQrHzM_Gig() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ShortcutInfo) obj).isPinnedVisible();
    }
}
