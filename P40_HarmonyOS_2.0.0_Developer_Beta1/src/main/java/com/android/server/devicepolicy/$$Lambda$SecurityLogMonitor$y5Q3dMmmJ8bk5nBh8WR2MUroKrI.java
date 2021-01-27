package com.android.server.devicepolicy;

import android.app.admin.SecurityLog;
import java.util.Comparator;

/* renamed from: com.android.server.devicepolicy.-$$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI implements Comparator {
    public static final /* synthetic */ $$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI INSTANCE = new $$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI();

    private /* synthetic */ $$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Long.signum(((SecurityLog.SecurityEvent) obj).getTimeNanos() - ((SecurityLog.SecurityEvent) obj2).getTimeNanos());
    }
}
