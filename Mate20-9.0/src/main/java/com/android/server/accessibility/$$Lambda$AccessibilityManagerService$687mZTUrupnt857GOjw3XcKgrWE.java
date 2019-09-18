package com.android.server.accessibility;

import android.os.RemoteCallbackList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE INSTANCE = new $$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE() {
    }

    public final void accept(Object obj, Object obj2) {
        ((AccessibilityManagerService) obj).sendServicesStateChanged((RemoteCallbackList) obj2);
    }
}
