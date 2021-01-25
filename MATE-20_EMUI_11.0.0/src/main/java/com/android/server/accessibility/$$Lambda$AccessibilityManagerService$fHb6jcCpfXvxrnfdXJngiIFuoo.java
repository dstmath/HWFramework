package com.android.server.accessibility;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnf-dXJngiIFuoo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo INSTANCE = new $$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AccessibilityManagerService) obj).sendAccessibilityButtonToInputFilter(((Integer) obj2).intValue());
    }
}
