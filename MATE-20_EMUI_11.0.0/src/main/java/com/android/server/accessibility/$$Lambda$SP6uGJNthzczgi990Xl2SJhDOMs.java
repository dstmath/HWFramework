package com.android.server.accessibility;

import android.graphics.Region;
import com.android.server.accessibility.MagnificationController;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs implements BiConsumer {
    public static final /* synthetic */ $$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs INSTANCE = new $$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs();

    private /* synthetic */ $$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MagnificationController.DisplayMagnification) obj).updateMagnificationRegion((Region) obj2);
    }
}
