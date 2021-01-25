package com.android.server.location;

import com.android.server.location.GnssConfiguration;

/* renamed from: com.android.server.location.-$$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao implements GnssConfiguration.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao INSTANCE = new $$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao();

    private /* synthetic */ $$Lambda$GnssConfiguration$1$9cfNUAWKKutp5KSqhvHSGJNe0ao() {
    }

    @Override // com.android.server.location.GnssConfiguration.SetCarrierProperty
    public final boolean set(int i) {
        return GnssConfiguration.native_set_supl_version(i);
    }
}
