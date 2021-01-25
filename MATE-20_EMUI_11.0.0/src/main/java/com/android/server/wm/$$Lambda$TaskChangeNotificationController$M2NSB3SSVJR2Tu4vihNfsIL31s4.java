package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4 implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4 INSTANCE = new $$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onPinnedStackAnimationStarted();
    }
}
