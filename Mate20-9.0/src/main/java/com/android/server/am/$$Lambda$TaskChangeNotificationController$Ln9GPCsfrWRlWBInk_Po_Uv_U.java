package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$Ln9-GPCsfrWRlWBInk_Po_Uv-_U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$Ln9GPCsfrWRlWBInk_Po_Uv_U implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$Ln9GPCsfrWRlWBInk_Po_Uv_U INSTANCE = new $$Lambda$TaskChangeNotificationController$Ln9GPCsfrWRlWBInk_Po_Uv_U();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$Ln9GPCsfrWRlWBInk_Po_Uv_U() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityLaunchOnSecondaryDisplayFailed();
    }
}
