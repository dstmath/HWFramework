package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw INSTANCE = new $$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskDisplayChanged(message.arg1, message.arg2);
    }
}
