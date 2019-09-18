package com.android.server.am;

import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$YDk9fnP8p2R_OweiU9rSGaheQeE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$YDk9fnP8p2R_OweiU9rSGaheQeE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$YDk9fnP8p2R_OweiU9rSGaheQeE INSTANCE = new $$Lambda$TaskChangeNotificationController$YDk9fnP8p2R_OweiU9rSGaheQeE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$YDk9fnP8p2R_OweiU9rSGaheQeE() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskCreated(message.arg1, (ComponentName) message.obj);
    }
}
