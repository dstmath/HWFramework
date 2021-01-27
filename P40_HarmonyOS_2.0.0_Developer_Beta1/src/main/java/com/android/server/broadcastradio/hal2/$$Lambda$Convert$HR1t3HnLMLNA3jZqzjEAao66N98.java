package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.ProgramIdentifier;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98 implements Function {
    public static final /* synthetic */ $$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98 INSTANCE = new $$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98();

    private /* synthetic */ $$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Convert.lambda$programInfoFromHal$3((ProgramIdentifier) obj);
    }
}
