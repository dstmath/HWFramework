package com.android.server.print;

import com.android.server.print.UserState;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$y51cj-jOuPNqkjzP4R89xJuclvo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$y51cjjOuPNqkjzP4R89xJuclvo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$y51cjjOuPNqkjzP4R89xJuclvo INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$y51cjjOuPNqkjzP4R89xJuclvo();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$y51cjjOuPNqkjzP4R89xJuclvo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchPrintersAdded((ArrayList) obj2);
    }
}
