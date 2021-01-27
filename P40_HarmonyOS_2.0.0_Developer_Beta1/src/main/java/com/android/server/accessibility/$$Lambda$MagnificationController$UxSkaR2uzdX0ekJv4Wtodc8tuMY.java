package com.android.server.accessibility;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY implements BiConsumer {
    public static final /* synthetic */ $$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY INSTANCE = new $$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY();

    private /* synthetic */ $$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MagnificationController) obj).resetAllIfNeeded(((Boolean) obj2).booleanValue());
    }
}
