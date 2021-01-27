package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM INSTANCE = new $$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onBackPressedOnTaskRoot((ActivityManager.RunningTaskInfo) message.obj);
    }
}
