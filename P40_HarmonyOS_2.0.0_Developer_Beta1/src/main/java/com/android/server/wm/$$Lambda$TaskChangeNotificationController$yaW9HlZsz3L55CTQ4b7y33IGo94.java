package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94 implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94 INSTANCE = new $$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityLaunchOnSecondaryDisplayFailed((ActivityManager.RunningTaskInfo) message.obj, message.arg1);
    }
}
