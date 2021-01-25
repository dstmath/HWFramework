package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE INSTANCE = new $$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskProfileLocked(message.arg1, message.arg2);
    }
}
