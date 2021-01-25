package com.android.server.backup.transport;

import com.android.server.backup.transport.TransportStats;
import java.util.function.BinaryOperator;

/* renamed from: com.android.server.backup.transport.-$$Lambda$bnpJn6l0a4iWMupJTDnTAfwT1eA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$bnpJn6l0a4iWMupJTDnTAfwT1eA implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$bnpJn6l0a4iWMupJTDnTAfwT1eA INSTANCE = new $$Lambda$bnpJn6l0a4iWMupJTDnTAfwT1eA();

    private /* synthetic */ $$Lambda$bnpJn6l0a4iWMupJTDnTAfwT1eA() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return TransportStats.Stats.merge((TransportStats.Stats) obj, (TransportStats.Stats) obj2);
    }
}
