package com.android.server;

import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.-$$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyA-s8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyAs8 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyAs8 INSTANCE = new $$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyAs8();

    private /* synthetic */ $$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyAs8() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((PinnerService) obj).pinApp(((Integer) obj2).intValue(), ((Integer) obj3).intValue(), ((Boolean) obj4).booleanValue());
    }
}
