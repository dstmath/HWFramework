package com.android.server.accessibility;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY INSTANCE = new $$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY();

    private /* synthetic */ $$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MagnificationController) obj).resetIfNeeded(((Integer) obj2).intValue(), ((Boolean) obj3).booleanValue());
    }
}
