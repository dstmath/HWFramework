package com.android.server.print;

import com.android.server.print.UserState;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$Ou3LUs53hzSrIma0FHPj2g3gePc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$Ou3LUs53hzSrIma0FHPj2g3gePc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$Ou3LUs53hzSrIma0FHPj2g3gePc INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$Ou3LUs53hzSrIma0FHPj2g3gePc();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$Ou3LUs53hzSrIma0FHPj2g3gePc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchCreatePrinterDiscoverySession((ArrayList) obj2);
    }
}
