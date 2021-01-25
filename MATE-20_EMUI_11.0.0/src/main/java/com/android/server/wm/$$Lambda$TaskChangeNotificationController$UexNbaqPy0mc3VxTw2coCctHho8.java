package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8 implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8 INSTANCE = new $$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskSnapshotChanged(message.arg1, (ActivityManager.TaskSnapshot) message.obj);
    }
}
