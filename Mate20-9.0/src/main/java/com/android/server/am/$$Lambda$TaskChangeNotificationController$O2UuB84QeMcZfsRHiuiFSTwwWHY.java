package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$O2UuB84QeMcZfsRHiuiFSTwwWHY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$O2UuB84QeMcZfsRHiuiFSTwwWHY implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$O2UuB84QeMcZfsRHiuiFSTwwWHY INSTANCE = new $$Lambda$TaskChangeNotificationController$O2UuB84QeMcZfsRHiuiFSTwwWHY();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$O2UuB84QeMcZfsRHiuiFSTwwWHY() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskRemovalStarted(message.arg1);
    }
}
