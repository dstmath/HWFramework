package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY INSTANCE = new $$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY();

    private /* synthetic */ $$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleStopPrinterDiscovery();
    }
}
