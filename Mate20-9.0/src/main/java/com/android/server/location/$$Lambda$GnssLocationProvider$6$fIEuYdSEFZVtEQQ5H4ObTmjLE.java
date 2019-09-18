package com.android.server.location;

import com.android.server.location.GnssLocationProvider;

/* renamed from: com.android.server.location.-$$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4O-bTmj-LE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4ObTmjLE implements GnssLocationProvider.SetCarrierProperty {
    public static final /* synthetic */ $$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4ObTmjLE INSTANCE = new $$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4ObTmjLE();

    private /* synthetic */ $$Lambda$GnssLocationProvider$6$fIEuYdSEFZVtEQQ5H4ObTmjLE() {
    }

    public final boolean set(int i) {
        return GnssLocationProvider.native_set_gnss_pos_protocol_select(i);
    }
}
