package com.android.server.autofill;

import java.util.function.Consumer;

/* renamed from: com.android.server.autofill.-$$Lambda$Session$cYu1t6lYVopApYW-vct82-7slZk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Session$cYu1t6lYVopApYWvct827slZk implements Consumer {
    public static final /* synthetic */ $$Lambda$Session$cYu1t6lYVopApYWvct827slZk INSTANCE = new $$Lambda$Session$cYu1t6lYVopApYWvct827slZk();

    private /* synthetic */ $$Lambda$Session$cYu1t6lYVopApYWvct827slZk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Session) obj).removeSelf();
    }
}
