package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HomePageDetect$-GEjqTSNaEEVeBATXdK3VySgon8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HomePageDetect$GEjqTSNaEEVeBATXdK3VySgon8 implements Function {
    public static final /* synthetic */ $$Lambda$HomePageDetect$GEjqTSNaEEVeBATXdK3VySgon8 INSTANCE = new $$Lambda$HomePageDetect$GEjqTSNaEEVeBATXdK3VySgon8();

    private /* synthetic */ $$Lambda$HomePageDetect$GEjqTSNaEEVeBATXdK3VySgon8() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ActivityStackEx) obj).getTopActivity();
    }
}
