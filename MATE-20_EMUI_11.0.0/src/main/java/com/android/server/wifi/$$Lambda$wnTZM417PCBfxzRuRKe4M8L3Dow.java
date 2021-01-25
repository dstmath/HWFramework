package com.android.server.wifi;

import com.android.server.wifi.LinkProbeManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.wifi.-$$Lambda$wnTZM417PCBfxzRuRKe4M8L3Dow  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$wnTZM417PCBfxzRuRKe4M8L3Dow implements Consumer {
    public static final /* synthetic */ $$Lambda$wnTZM417PCBfxzRuRKe4M8L3Dow INSTANCE = new $$Lambda$wnTZM417PCBfxzRuRKe4M8L3Dow();

    private /* synthetic */ $$Lambda$wnTZM417PCBfxzRuRKe4M8L3Dow() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((LinkProbeManager.Experiment) obj).resetOnScreenTurnedOn();
    }
}
