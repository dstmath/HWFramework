package com.android.server.wm;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.wm.TaskChangeNotificationController;

/* renamed from: com.android.server.wm.-$$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A implements TaskChangeNotificationController.TaskStackConsumer {
    public static final /* synthetic */ $$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A INSTANCE = new $$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A();

    private /* synthetic */ $$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A() {
    }

    @Override // com.android.server.wm.TaskChangeNotificationController.TaskStackConsumer
    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        iTaskStackListener.onTaskStackChanged();
    }
}
