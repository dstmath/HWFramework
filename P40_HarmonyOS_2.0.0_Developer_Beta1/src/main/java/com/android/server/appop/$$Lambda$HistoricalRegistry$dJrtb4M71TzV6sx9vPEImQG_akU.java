package com.android.server.appop;

import java.util.function.Consumer;

/* renamed from: com.android.server.appop.-$$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU implements Consumer {
    public static final /* synthetic */ $$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU INSTANCE = new $$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU();

    private /* synthetic */ $$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((HistoricalRegistry) obj).persistPendingHistory();
    }
}
