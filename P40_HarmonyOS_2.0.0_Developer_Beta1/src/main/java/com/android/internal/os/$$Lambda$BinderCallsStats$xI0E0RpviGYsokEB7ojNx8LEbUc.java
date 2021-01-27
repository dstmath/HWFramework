package com.android.internal.os;

import com.android.internal.os.BinderCallsStats;
import java.util.function.ToDoubleFunction;

/* renamed from: com.android.internal.os.-$$Lambda$BinderCallsStats$xI0E0RpviGYsokEB7ojNx8LEbUc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderCallsStats$xI0E0RpviGYsokEB7ojNx8LEbUc implements ToDoubleFunction {
    public static final /* synthetic */ $$Lambda$BinderCallsStats$xI0E0RpviGYsokEB7ojNx8LEbUc INSTANCE = new $$Lambda$BinderCallsStats$xI0E0RpviGYsokEB7ojNx8LEbUc();

    private /* synthetic */ $$Lambda$BinderCallsStats$xI0E0RpviGYsokEB7ojNx8LEbUc() {
    }

    @Override // java.util.function.ToDoubleFunction
    public final double applyAsDouble(Object obj) {
        return BinderCallsStats.lambda$dumpLocked$1((BinderCallsStats.UidEntry) obj);
    }
}
