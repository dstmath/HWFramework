package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I INSTANCE = new $$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityRequestedOrientationChanged(message.arg1, message.arg2);
    }
}
