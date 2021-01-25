package com.android.internal.telephony.dataconnection;

import java.net.InetAddress;
import java.util.function.Function;

/* renamed from: com.android.internal.telephony.dataconnection.-$$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0 implements Function {
    public static final /* synthetic */ $$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0 INSTANCE = new $$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0();

    private /* synthetic */ $$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((InetAddress) obj).getHostAddress();
    }
}
