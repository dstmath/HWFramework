package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA implements BiConsumer {
    public static final /* synthetic */ $$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA INSTANCE = new $$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA();

    private /* synthetic */ $$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((DisplayPolicy) obj).onVrStateChangedLw(((Boolean) obj2).booleanValue());
    }
}
