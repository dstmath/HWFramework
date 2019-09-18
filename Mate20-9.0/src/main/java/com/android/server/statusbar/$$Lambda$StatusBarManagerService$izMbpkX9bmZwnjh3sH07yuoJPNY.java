package com.android.server.statusbar;

import com.android.server.power.ShutdownThread;

/* renamed from: com.android.server.statusbar.-$$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY implements Runnable {
    public static final /* synthetic */ $$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY INSTANCE = new $$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY();

    private /* synthetic */ $$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY() {
    }

    public final void run() {
        ShutdownThread.shutdown(StatusBarManagerService.getUiContext(), "userrequested", false);
    }
}
