package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo INSTANCE = new $$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$sKzdHBM7V7DxdhcWx1u8hipJYFo() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_supl_es(i);
    }
}
