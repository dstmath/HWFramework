package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$OcNf5Tg_F4Us34H7SfC_e_tnGzw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$OcNf5Tg_F4Us34H7SfC_e_tnGzw implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$OcNf5Tg_F4Us34H7SfC_e_tnGzw INSTANCE = new $$Lambda$ShortcutService$OcNf5Tg_F4Us34H7SfC_e_tnGzw();

    private /* synthetic */ $$Lambda$ShortcutService$OcNf5Tg_F4Us34H7SfC_e_tnGzw() {
    }

    public final void accept(Object obj) {
        ((ShortcutUser) obj).detectLocaleChange();
    }
}
