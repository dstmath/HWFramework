package com.android.server.accessibility;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSF-M55-grAVbo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSFM55grAVbo implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSFM55grAVbo INSTANCE = new $$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSFM55grAVbo();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSFM55grAVbo() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AccessibilityManagerService) obj).sendStateToClients(((Integer) obj2).intValue(), ((Integer) obj3).intValue());
    }
}
