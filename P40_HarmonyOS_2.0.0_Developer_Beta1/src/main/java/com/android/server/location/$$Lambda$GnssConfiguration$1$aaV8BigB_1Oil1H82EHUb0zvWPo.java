package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo INSTANCE = new $$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$aaV8BigB_1Oil1H82EHUb0zvWPo() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_gnss_pos_protocol_select(i);
    }
}
