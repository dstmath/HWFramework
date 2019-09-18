package com.android.server.am;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$1RAH1a7gRlnrDczBty2_cTiNlBI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$1RAH1a7gRlnrDczBty2_cTiNlBI implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$1RAH1a7gRlnrDczBty2_cTiNlBI INSTANCE = new $$Lambda$TaskChangeNotificationController$1RAH1a7gRlnrDczBty2_cTiNlBI();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$1RAH1a7gRlnrDczBty2_cTiNlBI() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskSnapshotChanged(message.arg1, (ActivityManager.TaskSnapshot) message.obj);
    }
}
