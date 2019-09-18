package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$FNdlAMBaRkRCa4U_pc-uamD9VHw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$FNdlAMBaRkRCa4U_pcuamD9VHw implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$FNdlAMBaRkRCa4U_pcuamD9VHw INSTANCE = new $$Lambda$TaskChangeNotificationController$FNdlAMBaRkRCa4U_pcuamD9VHw();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$FNdlAMBaRkRCa4U_pcuamD9VHw() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskProfileLocked(message.arg1, message.arg2);
    }
}
