package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$5xMsPmGMl_n12-F1m2p9OBuXGrA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$5xMsPmGMl_n12F1m2p9OBuXGrA implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$5xMsPmGMl_n12F1m2p9OBuXGrA INSTANCE = new $$Lambda$TaskChangeNotificationController$5xMsPmGMl_n12F1m2p9OBuXGrA();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$5xMsPmGMl_n12F1m2p9OBuXGrA() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onPinnedStackAnimationEnded();
    }
}
