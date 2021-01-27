package com.android.server.print;

import android.print.PrintJobInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$tL9wtChZzY3dei-ul1VudkrPO20  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$tL9wtChZzY3deiul1VudkrPO20 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$tL9wtChZzY3deiul1VudkrPO20 INSTANCE = new $$Lambda$RemotePrintService$tL9wtChZzY3deiul1VudkrPO20();

    private /* synthetic */ $$Lambda$RemotePrintService$tL9wtChZzY3deiul1VudkrPO20() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).handleRequestCancelPrintJob((PrintJobInfo) obj2);
    }
}
