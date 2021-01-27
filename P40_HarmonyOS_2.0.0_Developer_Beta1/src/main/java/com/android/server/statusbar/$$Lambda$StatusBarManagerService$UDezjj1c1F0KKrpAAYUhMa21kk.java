package com.android.server.statusbar;

import com.android.server.power.ShutdownThread;

/* renamed from: com.android.server.statusbar.-$$Lambda$StatusBarManagerService$UDezjj1c1F0KKrp-AAYUhMa21kk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StatusBarManagerService$UDezjj1c1F0KKrpAAYUhMa21kk implements Runnable {
    public static final /* synthetic */ $$Lambda$StatusBarManagerService$UDezjj1c1F0KKrpAAYUhMa21kk INSTANCE = new $$Lambda$StatusBarManagerService$UDezjj1c1F0KKrpAAYUhMa21kk();

    private /* synthetic */ $$Lambda$StatusBarManagerService$UDezjj1c1F0KKrpAAYUhMa21kk() {
    }

    @Override // java.lang.Runnable
    public final void run() {
        ShutdownThread.shutdown(StatusBarManagerService.getUiContext(), "userrequested", false);
    }
}
