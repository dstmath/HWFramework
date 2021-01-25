package com.android.server.foldscreenview;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;

public class ActivityLifeStateMonitor extends IHwActivityNotifierEx {
    private static final String ACTIVITY_NOTIFY_COMPONENTNAME = "comp";
    private static final String ACTIVITY_NOTIFY_ONRESUME = "onResume";
    private static final String ACTIVITY_NOTIFY_REASON = "activityLifeState";
    private static final String ACTIVITY_NOTIFY_STATE = "state";
    private static final String LAUNCH_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final String STARTUP_GUIDE_PACKAGE_NAME = "com.huawei.hwstartupguide";
    private static final String TAG = "FoldScreen_ActivityLifeStateMonitor";
    private boolean mIsStartupGuideFlag = true;
    private SubScreenViewEntry mSubScreenViewEntry;

    public ActivityLifeStateMonitor(SubScreenViewEntry subScreenViewEntry) {
        this.mSubScreenViewEntry = subScreenViewEntry;
    }

    public void setStartupGuideFlag(boolean isStartupGuide) {
        this.mIsStartupGuideFlag = isStartupGuide;
    }

    public void call(Bundle extras) {
        ComponentName componentName;
        if (extras != null && (componentName = (ComponentName) extras.getParcelable(ACTIVITY_NOTIFY_COMPONENTNAME)) != null && ACTIVITY_NOTIFY_ONRESUME.equals(extras.getString(ACTIVITY_NOTIFY_STATE))) {
            Log.i(TAG, "activity lifestate componentName: " + componentName);
            if ("com.huawei.hwstartupguide".equals(componentName.getPackageName()) && this.mIsStartupGuideFlag) {
                this.mSubScreenViewEntry.handleStartupGuideChanged(true);
                this.mIsStartupGuideFlag = false;
            } else if ("com.huawei.android.launcher".equals(componentName.getPackageName())) {
                this.mSubScreenViewEntry.handleStartupGuideChanged(false);
                stop();
            } else {
                Log.i(TAG, "nothing need to do");
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
