package com.android.server.print;

import com.android.server.print.UserState;
import java.util.List;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$PrinterDiscoverySessionMediator$lfSsgTy_1NLRRkjOH_yL2Tk_x2w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$PrinterDiscoverySessionMediator$lfSsgTy_1NLRRkjOH_yL2Tk_x2w implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$lfSsgTy_1NLRRkjOH_yL2Tk_x2w INSTANCE = new $$Lambda$UserState$PrinterDiscoverySessionMediator$lfSsgTy_1NLRRkjOH_yL2Tk_x2w();

    private /* synthetic */ $$Lambda$UserState$PrinterDiscoverySessionMediator$lfSsgTy_1NLRRkjOH_yL2Tk_x2w() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState.PrinterDiscoverySessionMediator) obj).handleDispatchPrintersAdded((List) obj2);
    }
}
