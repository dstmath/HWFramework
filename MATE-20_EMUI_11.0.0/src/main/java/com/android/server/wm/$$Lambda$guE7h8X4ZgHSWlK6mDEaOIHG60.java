package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$guE7h8X4ZgHS-WlK6mDEaOIHG60  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$guE7h8X4ZgHSWlK6mDEaOIHG60 implements Consumer {
    public static final /* synthetic */ $$Lambda$guE7h8X4ZgHSWlK6mDEaOIHG60 INSTANCE = new $$Lambda$guE7h8X4ZgHSWlK6mDEaOIHG60();

    private /* synthetic */ $$Lambda$guE7h8X4ZgHSWlK6mDEaOIHG60() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((DisplayContent) obj).layoutAndAssignWindowLayersIfNeeded();
    }
}
