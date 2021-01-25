package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutPackage$9YSAfuJJkDxYR6ZL5AWyxpKsC_Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutPackage$9YSAfuJJkDxYR6ZL5AWyxpKsC_Y implements Predicate {
    public static final /* synthetic */ $$Lambda$ShortcutPackage$9YSAfuJJkDxYR6ZL5AWyxpKsC_Y INSTANCE = new $$Lambda$ShortcutPackage$9YSAfuJJkDxYR6ZL5AWyxpKsC_Y();

    private /* synthetic */ $$Lambda$ShortcutPackage$9YSAfuJJkDxYR6ZL5AWyxpKsC_Y() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ShortcutPackage.lambda$verifyStates$5((ShortcutInfo) obj);
    }
}
