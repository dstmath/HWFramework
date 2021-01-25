package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM INSTANCE = new $$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskMovedToFront((ActivityManager.RunningTaskInfo) message.obj);
    }
}
