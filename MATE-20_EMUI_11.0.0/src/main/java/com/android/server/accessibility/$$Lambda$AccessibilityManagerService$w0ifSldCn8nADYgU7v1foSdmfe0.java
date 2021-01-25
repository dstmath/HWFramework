package com.android.server.accessibility;

import com.android.server.accessibility.AccessibilityManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0 INSTANCE = new $$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AccessibilityManagerService) obj).updateInputFilter((AccessibilityManagerService.UserState) obj2);
    }
}
