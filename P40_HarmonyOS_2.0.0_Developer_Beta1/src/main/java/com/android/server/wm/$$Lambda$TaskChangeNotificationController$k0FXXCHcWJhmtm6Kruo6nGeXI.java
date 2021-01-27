package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$k0FXXC-HcWJhmtm6-Kruo6nGeXI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$k0FXXCHcWJhmtm6Kruo6nGeXI implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$k0FXXCHcWJhmtm6Kruo6nGeXI INSTANCE = new $$Lambda$TaskChangeNotificationController$k0FXXCHcWJhmtm6Kruo6nGeXI();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$k0FXXCHcWJhmtm6Kruo6nGeXI() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onPinnedStackAnimationEnded();
    }
}
