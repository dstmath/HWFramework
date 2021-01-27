package com.android.server.print;

import com.android.server.print.UserState;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$TAWPnRTK22Veu2-mmKNSJCvnBoU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$TAWPnRTK22Veu2mmKNSJCvnBoU implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$TAWPnRTK22Veu2mmKNSJCvnBoU INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$TAWPnRTK22Veu2mmKNSJCvnBoU();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$TAWPnRTK22Veu2mmKNSJCvnBoU() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchDestroyPrinterDiscoverySession((ArrayList) obj2);
    }
}
