package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M INSTANCE = new $$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskStackChanged();
    }
}
