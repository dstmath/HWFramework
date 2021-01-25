package com.android.server.wifi;

import com.android.server.wifi.LinkProbeManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.wifi.-$$Lambda$X1lFDUueUo45PAoqhGr4T3sqGcQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$X1lFDUueUo45PAoqhGr4T3sqGcQ implements Consumer {
    public static final /* synthetic */ $$Lambda$X1lFDUueUo45PAoqhGr4T3sqGcQ INSTANCE = new $$Lambda$X1lFDUueUo45PAoqhGr4T3sqGcQ();

    private /* synthetic */ $$Lambda$X1lFDUueUo45PAoqhGr4T3sqGcQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((LinkProbeManager.Experiment) obj).resetOnNewConnection();
    }
}
