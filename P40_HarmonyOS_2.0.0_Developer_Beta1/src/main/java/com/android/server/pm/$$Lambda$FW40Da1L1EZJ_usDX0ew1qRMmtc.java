package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$FW40Da1L1EZJ_usDX0ew1qRMmtc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FW40Da1L1EZJ_usDX0ew1qRMmtc implements Predicate {
    public static final /* synthetic */ $$Lambda$FW40Da1L1EZJ_usDX0ew1qRMmtc INSTANCE = new $$Lambda$FW40Da1L1EZJ_usDX0ew1qRMmtc();

    private /* synthetic */ $$Lambda$FW40Da1L1EZJ_usDX0ew1qRMmtc() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ShortcutInfo) obj).isManifestVisible();
    }
}
