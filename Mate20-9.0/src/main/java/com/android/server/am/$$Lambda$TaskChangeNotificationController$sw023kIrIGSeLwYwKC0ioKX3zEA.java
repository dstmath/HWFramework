package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$sw023kIrIGSeLwYwKC0ioKX3zEA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$sw023kIrIGSeLwYwKC0ioKX3zEA implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$sw023kIrIGSeLwYwKC0ioKX3zEA INSTANCE = new $$Lambda$TaskChangeNotificationController$sw023kIrIGSeLwYwKC0ioKX3zEA();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$sw023kIrIGSeLwYwKC0ioKX3zEA() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityForcedResizable((String) message.obj, message.arg1, message.arg2);
    }
}
