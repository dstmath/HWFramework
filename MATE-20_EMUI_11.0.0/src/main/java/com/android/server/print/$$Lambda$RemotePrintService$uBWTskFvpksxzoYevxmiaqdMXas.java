package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas INSTANCE = new $$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas();

    private /* synthetic */ $$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleBinderDied();
    }
}
