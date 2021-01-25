package com.android.server.print;

import com.android.server.print.UserState;
import java.util.List;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$CjemUQP8s7wG-dq-pIggj9Oze6I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$CjemUQP8s7wGdqpIggj9Oze6I implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$CjemUQP8s7wGdqpIggj9Oze6I INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$CjemUQP8s7wGdqpIggj9Oze6I();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$CjemUQP8s7wGdqpIggj9Oze6I() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchPrintersRemoved((List) obj2);
    }
}
