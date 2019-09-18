package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$qeFlXbEdNY-s36xnqPf5bs5axg0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$qeFlXbEdNYs36xnqPf5bs5axg0 implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$qeFlXbEdNYs36xnqPf5bs5axg0 INSTANCE = new $$Lambda$ShortcutService$qeFlXbEdNYs36xnqPf5bs5axg0();

    private /* synthetic */ $$Lambda$ShortcutService$qeFlXbEdNYs36xnqPf5bs5axg0() {
    }

    public final void accept(Object obj) {
        ((ShortcutPackageItem) obj).refreshPackageSignatureAndSave();
    }
}
