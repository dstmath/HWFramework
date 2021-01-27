package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$zP5AObb0-v-Zzwr-v8NXOg4Yt1c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zP5AObb0vZzwrv8NXOg4Yt1c implements BiConsumer {
    public static final /* synthetic */ $$Lambda$zP5AObb0vZzwrv8NXOg4Yt1c INSTANCE = new $$Lambda$zP5AObb0vZzwrv8NXOg4Yt1c();

    private /* synthetic */ $$Lambda$zP5AObb0vZzwrv8NXOg4Yt1c() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((WindowProcessListener) obj).setPendingUiClean(((Boolean) obj2).booleanValue());
    }
}
