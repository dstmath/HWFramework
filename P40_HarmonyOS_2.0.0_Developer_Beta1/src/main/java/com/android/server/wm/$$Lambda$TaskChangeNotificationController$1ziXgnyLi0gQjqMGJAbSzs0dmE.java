package com.android.server.wm;

import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0-dmE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0dmE implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0dmE INSTANCE = new $$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0dmE();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0dmE() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskCreated(message.arg1, (ComponentName) message.obj);
    }
}
