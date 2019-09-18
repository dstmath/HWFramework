package com.android.server.print;

import android.print.IPrinterDiscoveryObserver;
import com.android.internal.util.function.TriConsumer;
import com.android.server.print.UserState;
import java.util.ArrayList;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E implements TriConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handlePrintersAdded((IPrinterDiscoveryObserver) obj2, (ArrayList) obj3);
    }
}
