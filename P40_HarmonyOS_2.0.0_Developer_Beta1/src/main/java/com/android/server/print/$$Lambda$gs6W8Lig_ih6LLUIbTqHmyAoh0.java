package com.android.server.print;

import java.util.List;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$gs6W8Li-g_ih6LLUIbTqHmyAoh0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$gs6W8Lig_ih6LLUIbTqHmyAoh0 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$gs6W8Lig_ih6LLUIbTqHmyAoh0 INSTANCE = new $$Lambda$gs6W8Lig_ih6LLUIbTqHmyAoh0();

    private /* synthetic */ $$Lambda$gs6W8Lig_ih6LLUIbTqHmyAoh0() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).startPrinterDiscovery((List) obj2);
    }
}
