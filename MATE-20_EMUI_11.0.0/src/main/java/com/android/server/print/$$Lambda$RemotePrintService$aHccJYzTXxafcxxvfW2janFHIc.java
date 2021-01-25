package com.android.server.print;

import android.print.PrinterId;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$aHc-cJYzTXxafcxxvfW2janFHIc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$aHccJYzTXxafcxxvfW2janFHIc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$aHccJYzTXxafcxxvfW2janFHIc INSTANCE = new $$Lambda$RemotePrintService$aHccJYzTXxafcxxvfW2janFHIc();

    private /* synthetic */ $$Lambda$RemotePrintService$aHccJYzTXxafcxxvfW2janFHIc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).handleStartPrinterStateTracking((PrinterId) obj2);
    }
}
