package com.android.server.foldscreenview;

import android.content.ComponentName;
import android.os.Bundle;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;

public class AppSwitchMonitor extends IHwActivityNotifierEx {
    private static final String ACTIVITY_NOTIFY_FIELD_COMPONENT = "toActivity";
    private static final String ACTIVITY_NOTIFY_FIELD_FROM = "fromPackage";
    private static final String ACTIVITY_NOTIFY_FIELD_PID = "toPid";
    private static final String ACTIVITY_NOTIFY_REASON = "appSwitch";
    private static final String CAMERA_PACKAGENAME = "com.huawei.camera";
    private static final String TAG = "FoldScreen_SubScreenViewEntry";
    private SubScreenViewEntry mSubScreenViewEntry;

    public AppSwitchMonitor(SubScreenViewEntry subScreenViewEntry) {
        this.mSubScreenViewEntry = subScreenViewEntry;
    }

    public void call(Bundle extras) {
        ComponentName componentName;
        if (extras != null && (componentName = (ComponentName) extras.getParcelable(ACTIVITY_NOTIFY_FIELD_COMPONENT)) != null) {
            String fromPackage = extras.getString(ACTIVITY_NOTIFY_FIELD_FROM);
            String targetPackage = componentName.getPackageName();
            boolean isTargetCamera = CAMERA_PACKAGENAME.equals(targetPackage);
            boolean isFromCamera = CAMERA_PACKAGENAME.equals(fromPackage);
            this.mSubScreenViewEntry.handleForegroundAppChanged(extras.getInt(ACTIVITY_NOTIFY_FIELD_PID), targetPackage);
            if (isTargetCamera && !isFromCamera) {
                this.mSubScreenViewEntry.handleActivatedCamera(true);
            } else if (isFromCamera && !isTargetCamera) {
                this.mSubScreenViewEntry.handleActivatedCamera(false);
            }
        }
    }

    public void start() {
        ActivityManagerEx.registerHwActivityNotifier(this, ACTIVITY_NOTIFY_REASON);
    }

    public void stop() {
        ActivityManagerEx.unregisterHwActivityNotifier(this);
    }
}
