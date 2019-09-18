package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$bdUeaAkcePSCZBDxdJttl1FPOmI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$bdUeaAkcePSCZBDxdJttl1FPOmI implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$bdUeaAkcePSCZBDxdJttl1FPOmI INSTANCE = new $$Lambda$ShortcutService$bdUeaAkcePSCZBDxdJttl1FPOmI();

    private /* synthetic */ $$Lambda$ShortcutService$bdUeaAkcePSCZBDxdJttl1FPOmI() {
    }

    public final void accept(Object obj) {
        ((ShortcutPackage) obj).rescanPackageIfNeeded(false, true);
    }
}
