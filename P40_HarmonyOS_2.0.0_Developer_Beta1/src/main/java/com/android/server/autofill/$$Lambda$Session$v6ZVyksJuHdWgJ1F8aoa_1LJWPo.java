package com.android.server.autofill;

import java.util.function.Consumer;

/* renamed from: com.android.server.autofill.-$$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo implements Consumer {
    public static final /* synthetic */ $$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo INSTANCE = new $$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo();

    private /* synthetic */ $$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Session) obj).handleLogContextCommitted();
    }
}
