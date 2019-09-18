package com.android.server.am;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$bteC39aBoUFmJeWf3dk2BX1xZ6k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$bteC39aBoUFmJeWf3dk2BX1xZ6k implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$bteC39aBoUFmJeWf3dk2BX1xZ6k INSTANCE = new $$Lambda$TaskChangeNotificationController$bteC39aBoUFmJeWf3dk2BX1xZ6k();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$bteC39aBoUFmJeWf3dk2BX1xZ6k() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskDescriptionChanged(message.arg1, (ActivityManager.TaskDescription) message.obj);
    }
}
