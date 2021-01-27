package com.android.server;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.-$$Lambda$PinnerService$GeEX-8XoHeV0LEszxat7jOSlrs4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PinnerService$GeEX8XoHeV0LEszxat7jOSlrs4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PinnerService$GeEX8XoHeV0LEszxat7jOSlrs4 INSTANCE = new $$Lambda$PinnerService$GeEX8XoHeV0LEszxat7jOSlrs4();

    private /* synthetic */ $$Lambda$PinnerService$GeEX8XoHeV0LEszxat7jOSlrs4() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PinnerService) obj).pinApps(((Integer) obj2).intValue());
    }
}
