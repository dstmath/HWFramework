package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$fCl_JbVpr187Fh4_6N-IxgnU68c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$fCl_JbVpr187Fh4_6NIxgnU68c implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$fCl_JbVpr187Fh4_6NIxgnU68c INSTANCE = new $$Lambda$ShortcutService$fCl_JbVpr187Fh4_6NIxgnU68c();

    private /* synthetic */ $$Lambda$ShortcutService$fCl_JbVpr187Fh4_6NIxgnU68c() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutPackage) obj).refreshPinnedFlags();
    }
}
