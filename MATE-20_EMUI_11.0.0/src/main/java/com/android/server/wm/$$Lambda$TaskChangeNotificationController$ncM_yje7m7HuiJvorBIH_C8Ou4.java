package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$ncM_yje7-m7HuiJvorBIH_C8Ou4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$ncM_yje7m7HuiJvorBIH_C8Ou4 implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$ncM_yje7m7HuiJvorBIH_C8Ou4 INSTANCE = new $$Lambda$TaskChangeNotificationController$ncM_yje7m7HuiJvorBIH_C8Ou4();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$ncM_yje7m7HuiJvorBIH_C8Ou4() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityPinned((String) message.obj, message.sendingUid, message.arg1, message.arg2);
    }
}
