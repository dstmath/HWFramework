package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg INSTANCE = new $$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskRemovalStarted((ActivityManager.RunningTaskInfo) message.obj);
    }
}
