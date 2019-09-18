package com.android.server.print;

import android.print.PrinterId;
import com.android.internal.util.function.TriConsumer;
import com.android.server.print.UserState;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$iQrjLK8luujjjp1uW3VGCsAZK_g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$iQrjLK8luujjjp1uW3VGCsAZK_g implements TriConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$iQrjLK8luujjjp1uW3VGCsAZK_g INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$iQrjLK8luujjjp1uW3VGCsAZK_g();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$iQrjLK8luujjjp1uW3VGCsAZK_g() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleStartPrinterStateTracking((RemotePrintService) obj2, (PrinterId) obj3);
    }
}
