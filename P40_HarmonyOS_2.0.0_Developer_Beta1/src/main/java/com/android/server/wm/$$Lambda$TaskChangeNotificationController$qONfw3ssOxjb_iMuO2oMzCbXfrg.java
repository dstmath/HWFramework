package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg INSTANCE = new $$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityUnpinned();
    }
}
