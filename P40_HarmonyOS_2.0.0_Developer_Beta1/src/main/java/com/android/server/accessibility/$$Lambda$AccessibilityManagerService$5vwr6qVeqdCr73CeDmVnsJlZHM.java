package com.android.server.accessibility;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$5vwr6qV-eqdCr73CeDmVnsJlZHM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$5vwr6qVeqdCr73CeDmVnsJlZHM implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$5vwr6qVeqdCr73CeDmVnsJlZHM INSTANCE = new $$Lambda$AccessibilityManagerService$5vwr6qVeqdCr73CeDmVnsJlZHM();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$5vwr6qVeqdCr73CeDmVnsJlZHM() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AccessibilityManagerService) obj).sendStateToAllClients(((Integer) obj2).intValue(), ((Integer) obj3).intValue());
    }
}
