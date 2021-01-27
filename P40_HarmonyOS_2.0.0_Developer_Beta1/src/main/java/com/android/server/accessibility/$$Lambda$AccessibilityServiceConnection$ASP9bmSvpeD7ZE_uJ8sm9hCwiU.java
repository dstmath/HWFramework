package com.android.server.accessibility;

import java.util.function.Consumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm-9hCwiU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU implements Consumer {
    public static final /* synthetic */ $$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU INSTANCE = new $$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU();

    private /* synthetic */ $$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AccessibilityServiceConnection) obj).initializeService();
    }
}
