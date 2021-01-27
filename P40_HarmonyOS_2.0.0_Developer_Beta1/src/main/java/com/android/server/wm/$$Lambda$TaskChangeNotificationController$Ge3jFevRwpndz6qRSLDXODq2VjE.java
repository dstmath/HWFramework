package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE INSTANCE = new $$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskDescriptionChanged((ActivityManager.RunningTaskInfo) message.obj);
    }
}
