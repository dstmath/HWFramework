package com.android.server.location;

import android.os.IBinder;
import com.android.internal.location.ILocationProvider;
import com.android.server.ServiceWatcher;

/* renamed from: com.android.server.location.-$$Lambda$HwLocationProviderProxy$3xlds_S589E5DFHR9FdCv9qD-0g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwLocationProviderProxy$3xlds_S589E5DFHR9FdCv9qD0g implements ServiceWatcher.BlockingBinderRunner {
    public static final /* synthetic */ $$Lambda$HwLocationProviderProxy$3xlds_S589E5DFHR9FdCv9qD0g INSTANCE = new $$Lambda$HwLocationProviderProxy$3xlds_S589E5DFHR9FdCv9qD0g();

    private /* synthetic */ $$Lambda$HwLocationProviderProxy$3xlds_S589E5DFHR9FdCv9qD0g() {
    }

    public final Object run(IBinder iBinder) {
        return Long.valueOf(ILocationProvider.Stub.asInterface(iBinder).getStatusUpdateTime());
    }
}
