package com.android.server.accessibility;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g INSTANCE = new $$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AccessibilityManagerService) obj).showEnableTouchExplorationDialog((AccessibilityServiceConnection) obj2);
    }
}
