package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutService$oes_dY8CJz5MllJiOggarpV9YkA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutService$oes_dY8CJz5MllJiOggarpV9YkA implements Consumer {
    public static final /* synthetic */ $$Lambda$ShortcutService$oes_dY8CJz5MllJiOggarpV9YkA INSTANCE = new $$Lambda$ShortcutService$oes_dY8CJz5MllJiOggarpV9YkA();

    private /* synthetic */ $$Lambda$ShortcutService$oes_dY8CJz5MllJiOggarpV9YkA() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutUser) obj).detectLocaleChange();
    }
}
