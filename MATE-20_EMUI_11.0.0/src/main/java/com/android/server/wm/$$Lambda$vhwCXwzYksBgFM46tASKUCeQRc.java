package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$vhwCX-wzYksBgFM46tASKUCeQRc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$vhwCXwzYksBgFM46tASKUCeQRc implements Consumer {
    public static final /* synthetic */ $$Lambda$vhwCXwzYksBgFM46tASKUCeQRc INSTANCE = new $$Lambda$vhwCXwzYksBgFM46tASKUCeQRc();

    private /* synthetic */ $$Lambda$vhwCXwzYksBgFM46tASKUCeQRc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowState) obj).resetDragResizingChangeReported();
    }
}
