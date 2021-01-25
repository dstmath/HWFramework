package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO2-2BQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO22BQ implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO22BQ INSTANCE = new $$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO22BQ();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO22BQ() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        TaskChangeNotificationController.lambda$new$10(iTaskStackListener, message);
    }
}
