package com.android.server.accessibility;

import android.view.accessibility.IAccessibilityManagerClient;
import com.android.internal.util.FunctionalUtils;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE implements FunctionalUtils.RemoteExceptionIgnoringConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE INSTANCE = new $$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE() {
    }

    public final void acceptOrThrow(Object obj) {
        ((IAccessibilityManagerClient) obj).notifyServicesStateChanged();
    }
}
