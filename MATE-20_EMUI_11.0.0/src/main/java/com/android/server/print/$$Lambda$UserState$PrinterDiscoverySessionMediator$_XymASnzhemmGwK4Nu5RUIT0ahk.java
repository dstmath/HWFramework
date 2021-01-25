package com.android.server.print;

import android.print.PrinterId;
import com.android.internal.util.function.TriConsumer;
import com.android.server.print.UserState;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$_XymASnzhemmGwK4Nu5RUIT0ahk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$_XymASnzhemmGwK4Nu5RUIT0ahk implements TriConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$_XymASnzhemmGwK4Nu5RUIT0ahk INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$_XymASnzhemmGwK4Nu5RUIT0ahk();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$_XymASnzhemmGwK4Nu5RUIT0ahk() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleStopPrinterStateTracking((RemotePrintService) obj2, (PrinterId) obj3);
    }
}
