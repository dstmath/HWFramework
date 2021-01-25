package com.android.server.print;

import com.android.internal.util.function.TriConsumer;
import com.android.server.print.UserState;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$MT8AtQ4cegoEAucY7Fm8C8TCrjo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$MT8AtQ4cegoEAucY7Fm8C8TCrjo implements TriConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$MT8AtQ4cegoEAucY7Fm8C8TCrjo INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$MT8AtQ4cegoEAucY7Fm8C8TCrjo();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$MT8AtQ4cegoEAucY7Fm8C8TCrjo() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchStartPrinterDiscovery((ArrayList) obj2, (List) obj3);
    }
}
