package com.android.internal.telephony;

import android.telephony.PhysicalChannelConfig;
import java.util.function.Function;

/* renamed from: com.android.internal.telephony.-$$Lambda$WWHOcG5P4-jgjzPPgLwm-wN15OM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM implements Function {
    public static final /* synthetic */ $$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM INSTANCE = new $$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM();

    private /* synthetic */ $$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((PhysicalChannelConfig) obj).getCellBandwidthDownlink());
    }
}
