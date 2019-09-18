package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$YVmGNqlD5lzQCN49aly8kWWz1po  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$YVmGNqlD5lzQCN49aly8kWWz1po implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$YVmGNqlD5lzQCN49aly8kWWz1po INSTANCE = new $$Lambda$TaskChangeNotificationController$YVmGNqlD5lzQCN49aly8kWWz1po();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$YVmGNqlD5lzQCN49aly8kWWz1po() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        TaskChangeNotificationController.lambda$new$9(iTaskStackListener, message);
    }
}
