package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$XcHmyRxMY5ULhjLiV-sIKnPtvOM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XcHmyRxMY5ULhjLiVsIKnPtvOM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$XcHmyRxMY5ULhjLiVsIKnPtvOM INSTANCE = new $$Lambda$XcHmyRxMY5ULhjLiVsIKnPtvOM();

    private /* synthetic */ $$Lambda$XcHmyRxMY5ULhjLiVsIKnPtvOM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((DisplayPolicy) obj).setForceShowSystemBars(((Boolean) obj2).booleanValue());
    }
}
