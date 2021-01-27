package com.android.server.accessibility;

import java.util.function.Consumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$Gu-W_dQ2mWyy8l4tm19TzFxGbeM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM implements Consumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM INSTANCE = new $$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AccessibilityManagerService) obj).announceNewUserIfNeeded();
    }
}
