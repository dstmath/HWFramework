package com.android.server.om;

import android.os.IBinder;
import android.util.Slog;

/* renamed from: com.android.server.om.-$$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI implements IBinder.DeathRecipient {
    public static final /* synthetic */ $$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI INSTANCE = new $$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI();

    private /* synthetic */ $$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI() {
    }

    @Override // android.os.IBinder.DeathRecipient
    public final void binderDied() {
        Slog.w("OverlayManager", "service 'idmap' died");
    }
}
