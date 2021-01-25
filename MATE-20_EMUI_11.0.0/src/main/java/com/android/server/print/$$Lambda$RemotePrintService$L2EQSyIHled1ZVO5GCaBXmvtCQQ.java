package com.android.server.print;

import android.print.PrinterId;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ INSTANCE = new $$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ();

    private /* synthetic */ $$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).handleStopPrinterStateTracking((PrinterId) obj2);
    }
}
