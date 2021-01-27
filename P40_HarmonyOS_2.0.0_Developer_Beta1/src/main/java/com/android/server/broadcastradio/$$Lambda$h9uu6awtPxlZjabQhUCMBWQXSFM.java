package com.android.server.broadcastradio;

import android.hardware.radio.RadioManager;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.broadcastradio.-$$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM INSTANCE = new $$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM();

    private /* synthetic */ $$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((RadioManager.ModuleProperties) obj).getId();
    }
}
