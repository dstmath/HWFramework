package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ INSTANCE = new $$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ();

    private /* synthetic */ $$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleCreatePrinterDiscoverySession();
    }
}
