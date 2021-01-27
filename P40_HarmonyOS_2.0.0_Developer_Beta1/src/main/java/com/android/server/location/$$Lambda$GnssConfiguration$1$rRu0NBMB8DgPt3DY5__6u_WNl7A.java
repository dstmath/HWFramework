package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A INSTANCE = new $$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$rRu0NBMB8DgPt3DY5__6u_WNl7A() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_gps_lock(i);
    }
}
