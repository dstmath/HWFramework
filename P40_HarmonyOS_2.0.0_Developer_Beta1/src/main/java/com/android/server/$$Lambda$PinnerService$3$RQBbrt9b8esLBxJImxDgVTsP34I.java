package com.android.server;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.-$$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I INSTANCE = new $$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I();

    private /* synthetic */ $$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PinnerService) obj).handleUidGone(((Integer) obj2).intValue());
    }
}
