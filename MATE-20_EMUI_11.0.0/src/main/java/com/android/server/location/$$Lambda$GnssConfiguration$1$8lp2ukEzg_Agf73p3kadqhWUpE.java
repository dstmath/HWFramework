package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3ka-dqhWUpE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3kadqhWUpE implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3kadqhWUpE INSTANCE = new $$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3kadqhWUpE();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$8lp2ukEzg_Agf73p3kadqhWUpE() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_emergency_supl_pdn(i);
    }
}
