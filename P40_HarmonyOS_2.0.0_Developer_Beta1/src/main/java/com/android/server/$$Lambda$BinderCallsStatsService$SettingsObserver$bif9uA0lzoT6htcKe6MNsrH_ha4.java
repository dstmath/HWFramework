package com.android.server;

import android.os.Binder;
import com.android.internal.os.BinderInternal;

/* renamed from: com.android.server.-$$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4 implements BinderInternal.WorkSourceProvider {
    public static final /* synthetic */ $$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4 INSTANCE = new $$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4();

    private /* synthetic */ $$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4() {
    }

    public final int resolveWorkSourceUid(int i) {
        return Binder.getCallingUid();
    }
}
