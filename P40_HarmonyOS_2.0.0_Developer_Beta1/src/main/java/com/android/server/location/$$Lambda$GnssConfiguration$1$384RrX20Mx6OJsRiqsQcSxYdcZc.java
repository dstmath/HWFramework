package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc INSTANCE = new $$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$384RrX20Mx6OJsRiqsQcSxYdcZc() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_supl_mode(i);
    }
}
