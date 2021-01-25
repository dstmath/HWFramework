package com.android.server.accessibility;

import android.view.accessibility.AccessibilityEvent;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY implements TriConsumer {
    public static final /* synthetic */ $$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY INSTANCE = new $$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY();

    private /* synthetic */ $$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AccessibilityManagerService) obj).sendAccessibilityEvent((AccessibilityEvent) obj2, ((Integer) obj3).intValue());
    }
}
