package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$0m_-qN9QkcgkoWun2Biw8le4l1Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$0m_qN9QkcgkoWun2Biw8le4l1Y implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$0m_qN9QkcgkoWun2Biw8le4l1Y INSTANCE = new $$Lambda$TaskChangeNotificationController$0m_qN9QkcgkoWun2Biw8le4l1Y();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$0m_qN9QkcgkoWun2Biw8le4l1Y() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityDismissingDockedStack();
    }
}
