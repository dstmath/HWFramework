package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$grn5FwM5ofT98exjpSvrJhz-e7s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$grn5FwM5ofT98exjpSvrJhze7s implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$grn5FwM5ofT98exjpSvrJhze7s INSTANCE = new $$Lambda$TaskChangeNotificationController$grn5FwM5ofT98exjpSvrJhze7s();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$grn5FwM5ofT98exjpSvrJhze7s() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityRequestedOrientationChanged(message.arg1, message.arg2);
    }
}
