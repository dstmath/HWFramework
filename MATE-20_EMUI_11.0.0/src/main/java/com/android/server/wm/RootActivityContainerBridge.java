package com.android.server.wm;

import android.app.ActivityOptions;
import com.huawei.server.wm.IHwRootActivityContainerEx;

public class RootActivityContainerBridge implements IHwRootActivityContainerEx {
    private RootActivityContainerBridgeEx mRootActivityContainerBridgeEx;

    public RootActivityContainerBridge(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx serviceEx) {
    }

    public RootActivityContainerBridgeEx getRootActivityContainerBridgeEx() {
        return this.mRootActivityContainerBridgeEx;
    }

    public void setRootActivityContainerBridge(RootActivityContainerBridgeEx bridgeEx) {
        this.mRootActivityContainerBridgeEx = bridgeEx;
    }

    public boolean resumeAppLockActivityIfNeeded(ActivityStack stack, ActivityOptions targetOptions) {
        ActivityStackEx activityStackEx = null;
        if (stack != null) {
            activityStackEx = new ActivityStackEx();
            activityStackEx.setActivityStack(stack);
        }
        return this.mRootActivityContainerBridgeEx.resumeAppLockActivityIfNeeded(activityStackEx, targetOptions);
    }

    public boolean isAppInLockList(String pgkName, int userId) {
        return this.mRootActivityContainerBridgeEx.isAppInLockList(pgkName, userId);
    }

    public void checkStartAppLockActivity() {
        this.mRootActivityContainerBridgeEx.checkStartAppLockActivity();
    }

    public boolean checkWindowModeForAppLock(ActivityRecord target, ActivityRecord activityRecord) {
        return this.mRootActivityContainerBridgeEx.checkWindowModeForAppLock(new ActivityRecordEx(target), new ActivityRecordEx(activityRecord));
    }
}
