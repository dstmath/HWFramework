package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$a1rNhcYLIsgLeCng0_osaimgbqE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$a1rNhcYLIsgLeCng0_osaimgbqE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$a1rNhcYLIsgLeCng0_osaimgbqE INSTANCE = new $$Lambda$TaskChangeNotificationController$a1rNhcYLIsgLeCng0_osaimgbqE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$a1rNhcYLIsgLeCng0_osaimgbqE() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityPinned((String) message.obj, message.sendingUid, message.arg1, message.arg2);
    }
}
