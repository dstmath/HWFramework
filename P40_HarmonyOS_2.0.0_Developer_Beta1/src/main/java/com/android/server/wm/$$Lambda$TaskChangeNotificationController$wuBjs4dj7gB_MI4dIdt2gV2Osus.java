package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus INSTANCE = new $$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityLaunchOnSecondaryDisplayRerouted((ActivityManager.RunningTaskInfo) message.obj, message.arg1);
    }
}
