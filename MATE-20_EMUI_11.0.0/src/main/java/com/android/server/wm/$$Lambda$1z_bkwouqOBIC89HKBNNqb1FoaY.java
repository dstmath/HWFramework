package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY implements BiConsumer {
    public static final /* synthetic */ $$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY INSTANCE = new $$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY();

    private /* synthetic */ $$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((DisplayPolicy) obj).setPointerLocationEnabled(((Boolean) obj2).booleanValue());
    }
}
