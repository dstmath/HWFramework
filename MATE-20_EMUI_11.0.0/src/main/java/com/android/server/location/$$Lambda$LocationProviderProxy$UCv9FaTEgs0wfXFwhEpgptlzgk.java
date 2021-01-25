package com.android.server.location;

import android.os.IBinder;
import com.android.internal.location.ILocationProvider;
import com.android.server.ServiceWatcher;

/* renamed from: com.android.server.location.-$$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzg-k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzgk implements ServiceWatcher.BlockingBinderRunner {
    public static final /* synthetic */ $$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzgk INSTANCE = new $$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzgk();

    private /* synthetic */ $$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzgk() {
    }

    @Override // com.android.server.ServiceWatcher.BlockingBinderRunner
    public final Object run(IBinder iBinder) {
        return Long.valueOf(ILocationProvider.Stub.asInterface(iBinder).getStatusUpdateTime());
    }
}
