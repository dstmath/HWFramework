package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$exGcjcSQADxpLL30XenIn9sDxlI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$exGcjcSQADxpLL30XenIn9sDxlI implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$exGcjcSQADxpLL30XenIn9sDxlI INSTANCE = new $$Lambda$ShortcutService$exGcjcSQADxpLL30XenIn9sDxlI();

    private /* synthetic */ $$Lambda$ShortcutService$exGcjcSQADxpLL30XenIn9sDxlI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutLauncher) obj).ensurePackageInfo();
    }
}
