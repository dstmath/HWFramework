package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.IBinder;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA INSTANCE = new $$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onSizeCompatModeActivityChanged(message.arg1, (IBinder) message.obj);
    }
}
