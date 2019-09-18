package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$TaskStack$n0sDe5GcitIQB-Orca4W45Hcc98  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskStack$n0sDe5GcitIQBOrca4W45Hcc98 implements Consumer {
    public static final /* synthetic */ $$Lambda$TaskStack$n0sDe5GcitIQBOrca4W45Hcc98 INSTANCE = new $$Lambda$TaskStack$n0sDe5GcitIQBOrca4W45Hcc98();

    private /* synthetic */ $$Lambda$TaskStack$n0sDe5GcitIQBOrca4W45Hcc98() {
    }

    public final void accept(Object obj) {
        ((WindowState) obj).mWinAnimator.resetDrawState();
    }
}
