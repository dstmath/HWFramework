package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2A-CA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2ACA implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2ACA INSTANCE = new $$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2ACA();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$5tBf0Ru8L994vqKbXOeOBj2ACA() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_lpp_profile(i);
    }
}
