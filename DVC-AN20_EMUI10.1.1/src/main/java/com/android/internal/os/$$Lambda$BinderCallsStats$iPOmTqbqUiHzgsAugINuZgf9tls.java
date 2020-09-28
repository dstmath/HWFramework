package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.function.ToDoubleFunction;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$iPOmTqbqUiHzgsAugINuZgf9tls  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$iPOmTqbqUiHzgsAugINuZgf9tls implements ToDoubleFunction {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$iPOmTqbqUiHzgsAugINuZgf9tls INSTANCE = new $$Lambda$BinderCallsStats$iPOmTqbqUiHzgsAugINuZgf9tls();

    private /* synthetic */ $$Lambda$BinderCallsStats$iPOmTqbqUiHzgsAugINuZgf9tls() {
    }

    @Override // java.util.function.ToDoubleFunction
    public final double applyAsDouble(Object obj) {
        return BinderCallsStats.lambda$dumpLocked$0((BinderCallsStats.UidEntry) obj);
    }
}
