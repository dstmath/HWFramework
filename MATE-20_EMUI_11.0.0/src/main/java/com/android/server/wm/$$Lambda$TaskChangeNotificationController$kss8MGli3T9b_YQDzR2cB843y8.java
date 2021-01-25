package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$kss8MGli3T9b_Y-QDzR2cB843y8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$kss8MGli3T9b_YQDzR2cB843y8 implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$kss8MGli3T9b_YQDzR2cB843y8 INSTANCE = new $$Lambda$TaskChangeNotificationController$kss8MGli3T9b_YQDzR2cB843y8();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$kss8MGli3T9b_YQDzR2cB843y8() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskRemoved(message.arg1);
    }
}
