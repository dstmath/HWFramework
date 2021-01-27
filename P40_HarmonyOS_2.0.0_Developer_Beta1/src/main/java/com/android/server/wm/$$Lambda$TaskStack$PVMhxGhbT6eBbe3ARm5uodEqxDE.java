package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE implements Consumer {
    public static final /* synthetic */ $$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE INSTANCE = new $$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE();

    private /* synthetic */ $$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowState) obj).mWinAnimator.setOffsetPositionForStackResize(true);
    }
}
