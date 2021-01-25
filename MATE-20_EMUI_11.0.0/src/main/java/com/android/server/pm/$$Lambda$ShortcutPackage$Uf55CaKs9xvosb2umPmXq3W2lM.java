package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutPackage$Uf55CaKs9xv-osb2umPmXq3W2lM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutPackage$Uf55CaKs9xvosb2umPmXq3W2lM implements Predicate {
    public static final /* synthetic */ $$Lambda$ShortcutPackage$Uf55CaKs9xvosb2umPmXq3W2lM INSTANCE = new $$Lambda$ShortcutPackage$Uf55CaKs9xvosb2umPmXq3W2lM();

    private /* synthetic */ $$Lambda$ShortcutPackage$Uf55CaKs9xvosb2umPmXq3W2lM() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ShortcutPackage.lambda$verifyStates$4((ShortcutInfo) obj);
    }
}
