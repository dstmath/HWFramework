package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw INSTANCE = new $$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityForcedResizable((String) message.obj, message.arg1, message.arg2);
    }
}
