package com.android.server.print;

import android.print.PrinterId;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$qhnzLVwIUlj5cUdZ9YacT2IXyug  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$qhnzLVwIUlj5cUdZ9YacT2IXyug implements BiConsumer {
    public static final /* synthetic */ $$Lambda$qhnzLVwIUlj5cUdZ9YacT2IXyug INSTANCE = new $$Lambda$qhnzLVwIUlj5cUdZ9YacT2IXyug();

    private /* synthetic */ $$Lambda$qhnzLVwIUlj5cUdZ9YacT2IXyug() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).startPrinterStateTracking((PrinterId) obj2);
    }
}
