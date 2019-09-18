package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$IPqcWaWHIL4UnZEYJhAve5H7KmE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$IPqcWaWHIL4UnZEYJhAve5H7KmE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$IPqcWaWHIL4UnZEYJhAve5H7KmE INSTANCE = new $$Lambda$TaskChangeNotificationController$IPqcWaWHIL4UnZEYJhAve5H7KmE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$IPqcWaWHIL4UnZEYJhAve5H7KmE() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskMovedToFront(message.arg1);
    }
}
