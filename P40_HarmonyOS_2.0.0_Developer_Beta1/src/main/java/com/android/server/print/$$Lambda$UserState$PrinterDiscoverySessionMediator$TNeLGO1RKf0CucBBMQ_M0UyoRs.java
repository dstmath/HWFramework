package com.android.server.print;

import com.android.server.print.UserState;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$TNeLGO1RKf0CucB-BMQ_M0UyoRs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$TNeLGO1RKf0CucBBMQ_M0UyoRs implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$TNeLGO1RKf0CucBBMQ_M0UyoRs INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$TNeLGO1RKf0CucBBMQ_M0UyoRs();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$TNeLGO1RKf0CucBBMQ_M0UyoRs() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchStopPrinterDiscovery((ArrayList) obj2);
    }
}
