package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk implements BiConsumer {
    public static final /* synthetic */ $$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk INSTANCE = new $$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk();

    private /* synthetic */ $$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((DisplayPolicy) obj).onLockTaskStateChangedLw(((Integer) obj2).intValue());
    }
}
