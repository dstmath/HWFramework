package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$l8T8kXBB-Gktym0FoX_WiKj2Glc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$l8T8kXBBGktym0FoX_WiKj2Glc implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$l8T8kXBBGktym0FoX_WiKj2Glc INSTANCE = new $$Lambda$ShortcutService$l8T8kXBBGktym0FoX_WiKj2Glc();

    private /* synthetic */ $$Lambda$ShortcutService$l8T8kXBBGktym0FoX_WiKj2Glc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutPackageItem) obj).refreshPackageSignatureAndSave();
    }
}
