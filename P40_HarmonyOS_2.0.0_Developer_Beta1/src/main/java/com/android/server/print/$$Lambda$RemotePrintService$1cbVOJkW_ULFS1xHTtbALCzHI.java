package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$1cbVOJkW_ULFS1xH-T-tbALCzHI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$1cbVOJkW_ULFS1xHTtbALCzHI implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$1cbVOJkW_ULFS1xHTtbALCzHI INSTANCE = new $$Lambda$RemotePrintService$1cbVOJkW_ULFS1xHTtbALCzHI();

    private /* synthetic */ $$Lambda$RemotePrintService$1cbVOJkW_ULFS1xHTtbALCzHI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleOnAllPrintJobsHandled();
    }
}
