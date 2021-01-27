package com.android.server.wm;

import android.app.ActivityOptions;

public class RootActivityContainerBridgeEx {
    protected static final boolean DEBUG_STATES = ActivityTaskManagerDebugConfig.DEBUG_STATES;
    private RootActivityContainerBridge mRootActivityContainerBridge;

    public RootActivityContainerBridgeEx(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx serviceEx) {
        this.mRootActivityContainerBridge = new RootActivityContainerBridge(rac, serviceEx);
        this.mRootActivityContainerBridge.setRootActivityContainerBridge(this);
    }

    public RootActivityContainerBridge getRootActivityContainerBridge() {
        return this.mRootActivityContainerBridge;
    }

    public boolean resumeAppLockActivityIfNeeded(ActivityStackEx stack, ActivityOptions targetOptions, boolean ignoreResumed) {
        return false;
    }

    public void removeStartingAppLockTaskId(int stackId) {
    }

    public boolean isAppInLockList(String pgkName, int userId) {
        return false;
    }

    public void checkStartAppLockActivity() {
    }

    public boolean checkWindowModeForAppLock(ActivityRecordEx target, ActivityRecordEx r) {
        return false;
    }
}
