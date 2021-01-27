package com.android.server.am;

import android.os.RemoteCallbackList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.am.-$$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0 INSTANCE = new $$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0();

    private /* synthetic */ $$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PendingIntentController) obj).handlePendingIntentCancelled((RemoteCallbackList) obj2);
    }
}
