package com.android.server.location;

import com.android.server.location.GnssLocationProvider;

/* renamed from: com.android.server.location.-$$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI implements GnssLocationProvider.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI INSTANCE = new $$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI();

    private /* synthetic */ $$Lambda$GnssLocationProvider$6$0TBIDASC8cGFJxhCk2blveu19LI() {
    }

    public final boolean set(int i) {
        return GnssLocationProvider.native_set_gps_lock(i);
    }
}
