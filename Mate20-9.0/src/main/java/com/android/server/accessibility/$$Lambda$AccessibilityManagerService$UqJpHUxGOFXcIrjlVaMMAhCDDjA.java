package com.android.server.accessibility;

import java.util.function.Consumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA implements Consumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA INSTANCE = new $$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA() {
    }

    public final void accept(Object obj) {
        ((AccessibilityManagerService) obj).sendAccessibilityButtonToInputFilter();
    }
}
