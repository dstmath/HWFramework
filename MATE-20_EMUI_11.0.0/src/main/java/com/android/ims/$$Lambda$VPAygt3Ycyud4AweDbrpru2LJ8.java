package com.android.ims;

import com.android.ims.MmTelFeatureConnection;
import java.util.function.Consumer;

/* renamed from: com.android.ims.-$$Lambda$VPAygt3Y-cyud4AweDbrpru2LJ8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VPAygt3Ycyud4AweDbrpru2LJ8 implements Consumer {
    public static final /* synthetic */ $$Lambda$VPAygt3Ycyud4AweDbrpru2LJ8 INSTANCE = new $$Lambda$VPAygt3Ycyud4AweDbrpru2LJ8();

    private /* synthetic */ $$Lambda$VPAygt3Ycyud4AweDbrpru2LJ8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((MmTelFeatureConnection.IFeatureUpdate) obj).notifyUnavailable();
    }
}
