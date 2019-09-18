package com.android.server.location;

import com.android.server.location.GnssLocationProvider;

/* renamed from: com.android.server.location.-$$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPs-lc4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPslc4 implements GnssLocationProvider.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPslc4 INSTANCE = new $$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPslc4();

    private /* synthetic */ $$Lambda$GnssLocationProvider$6$M4Zfb6dp_EFsOdGGju4tOPslc4() {
    }

    public final boolean set(int i) {
        return GnssLocationProvider.native_set_emergency_supl_pdn(i);
    }
}
