package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$WindowManagerService$TnTStANBpfyYgSfVdEMpwOZMj2Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WindowManagerService$TnTStANBpfyYgSfVdEMpwOZMj2Y implements Consumer {
    public static final /* synthetic */ $$Lambda$WindowManagerService$TnTStANBpfyYgSfVdEMpwOZMj2Y INSTANCE = new $$Lambda$WindowManagerService$TnTStANBpfyYgSfVdEMpwOZMj2Y();

    private /* synthetic */ $$Lambda$WindowManagerService$TnTStANBpfyYgSfVdEMpwOZMj2Y() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((DisplayContent) obj).getInputMonitor().updateInputWindowsImmediately();
    }
}
