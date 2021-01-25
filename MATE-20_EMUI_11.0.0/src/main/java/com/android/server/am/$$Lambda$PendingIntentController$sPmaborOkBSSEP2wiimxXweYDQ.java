package com.android.server.am;

import android.os.IBinder;
import com.android.internal.util.function.TriConsumer;
import java.lang.ref.WeakReference;

/* renamed from: com.android.server.am.-$$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXw-eYDQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ implements TriConsumer {
    public static final /* synthetic */ $$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ INSTANCE = new $$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ();

    private /* synthetic */ $$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PendingIntentController) obj).clearPendingResultForActivity((IBinder) obj2, (WeakReference) obj3);
    }
}
