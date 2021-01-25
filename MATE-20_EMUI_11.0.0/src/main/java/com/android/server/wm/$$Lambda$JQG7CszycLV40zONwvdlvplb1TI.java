package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$JQG7CszycLV40zONwvdlvplb1TI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$JQG7CszycLV40zONwvdlvplb1TI implements Consumer {
    public static final /* synthetic */ $$Lambda$JQG7CszycLV40zONwvdlvplb1TI INSTANCE = new $$Lambda$JQG7CszycLV40zONwvdlvplb1TI();

    private /* synthetic */ $$Lambda$JQG7CszycLV40zONwvdlvplb1TI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((DisplayContent) obj).updateSystemGestureExclusionLimit();
    }
}
