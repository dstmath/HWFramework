package com.android.server.accessibility;

import com.android.server.accessibility.AccessibilityManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo INSTANCE = new $$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AccessibilityManagerService) obj).updateFingerprintGestureHandling((AccessibilityManagerService.UserState) obj2);
    }
}
