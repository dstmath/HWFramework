package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController;

/* renamed from: com.android.server.am.-$$Lambda$TaskChangeNotificationController$d9Depygk2x7Vm_pl1RSk9_SSjvA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$d9Depygk2x7Vm_pl1RSk9_SSjvA implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$d9Depygk2x7Vm_pl1RSk9_SSjvA INSTANCE = new $$Lambda$TaskChangeNotificationController$d9Depygk2x7Vm_pl1RSk9_SSjvA();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$d9Depygk2x7Vm_pl1RSk9_SSjvA() {
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onActivityDismissingDockedStack();
    }
}
