package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0W-M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0WM implements Consumer {
    public static final /* synthetic */ $$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0WM INSTANCE = new $$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0WM();

    private /* synthetic */ $$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0WM() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((DisplayPolicy) obj).resetSystemUiVisibilityLw();
    }
}
