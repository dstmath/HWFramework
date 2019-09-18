package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$K9kaSj6_p5pzfyRh9i93xiC9T3s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$K9kaSj6_p5pzfyRh9i93xiC9T3s implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$K9kaSj6_p5pzfyRh9i93xiC9T3s INSTANCE = new $$Lambda$TaskChangeNotificationController$K9kaSj6_p5pzfyRh9i93xiC9T3s();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$K9kaSj6_p5pzfyRh9i93xiC9T3s() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskRemoved(message.arg1);
    }
}
