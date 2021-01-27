package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4 implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4 INSTANCE = new $$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4();

    private /* synthetic */ $$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleDestroyPrinterDiscoverySession();
    }
}
