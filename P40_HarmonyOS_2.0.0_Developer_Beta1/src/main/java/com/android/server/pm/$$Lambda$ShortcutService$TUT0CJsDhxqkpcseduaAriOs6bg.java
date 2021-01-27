package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$TUT0CJsDhxqkpcseduaAriOs6bg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$TUT0CJsDhxqkpcseduaAriOs6bg implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$TUT0CJsDhxqkpcseduaAriOs6bg INSTANCE = new $$Lambda$ShortcutService$TUT0CJsDhxqkpcseduaAriOs6bg();

    private /* synthetic */ $$Lambda$ShortcutService$TUT0CJsDhxqkpcseduaAriOs6bg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutPackage) obj).rescanPackageIfNeeded(false, true);
    }
}
