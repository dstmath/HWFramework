package com.android.server.foldscreenview;

import android.os.Bundle;
import android.os.Handler;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;

public class ActivityMonitor extends IHwActivityNotifierEx {
    private static final String ACTIVITY_NOTIFY_REASON = "returnToHome";
    private Handler mNotifyHandler;
    private int mNotifyMsgId;
    private String mPackageNameOfRemoteViews;

    public ActivityMonitor(Handler notifyHandler, int notifyMsgId) {
        this.mNotifyHandler = notifyHandler;
        this.mNotifyMsgId = notifyMsgId;
    }

    public void setPackageNameOfRemoteViews(String packageName) {
        this.mPackageNameOfRemoteViews = packageName;
    }

    public void call(Bundle extras) {
        if (extras != null && HwFoldScreenManagerEx.getDisplayMode() == 3) {
            this.mNotifyHandler.sendMessage(this.mNotifyHandler.obtainMessage(this.mNotifyMsgId, this.mPackageNameOfRemoteViews));
        }
    }

    public void start() {
        ActivityManagerEx.registerHwActivityNotifier(this, ACTIVITY_NOTIFY_REASON);
    }

    public void stop() {
        ActivityManagerEx.unregisterHwActivityNotifier(this);
    }
}
