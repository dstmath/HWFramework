package com.android.server.wm;

import android.app.ActivityManagerInternal;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ADNhW0r9Skcs9ezrOGURijI-lyQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ INSTANCE = new $$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ();

    private /* synthetic */ $$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityManagerInternal) obj).updateOomLevelsForDisplay(((Integer) obj2).intValue());
    }
}
