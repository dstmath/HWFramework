package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.DabTableEntry;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0 implements Function {
    public static final /* synthetic */ $$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0 INSTANCE = new $$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0();

    private /* synthetic */ $$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((DabTableEntry) obj).frequency);
    }
}
