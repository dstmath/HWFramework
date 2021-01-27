package com.android.server.wm;

import android.graphics.Rect;
import android.os.IBinder;

public class AppWindowTokenExt {
    private AppWindowToken mAppWindowToken;

    public AppWindowTokenExt(AppWindowToken appWindowToken) {
        this.mAppWindowToken = appWindowToken;
    }

    public AppWindowTokenExt() {
    }

    public AppWindowToken getAppWindowToken() {
        return this.mAppWindowToken;
    }

    public void setAppWindowToken(AppWindowToken appWindowToken) {
        this.mAppWindowToken = appWindowToken;
    }

    public void resetAppWindowToken(Object appWindowToken) {
        if (appWindowToken != null && (appWindowToken instanceof AppWindowToken)) {
            this.mAppWindowToken = (AppWindowToken) appWindowToken;
        }
    }

    public int getOrientation() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            return -1;
        }
        return appWindowToken.mOrientation;
    }

    public boolean getSendingToBottom() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            return false;
        }
        return appWindowToken.sendingToBottom;
    }

    public boolean getIsVisible() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.isVisible();
        }
        return false;
    }

    public WindowStateEx findMainWindow() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.findMainWindow() == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(this.mAppWindowToken.findMainWindow());
        return windowStateEx;
    }

    public boolean fillsParent() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            return false;
        }
        return appWindowToken.fillsParent();
    }

    public IBinder getAppToken() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.appToken;
        }
        return null;
    }

    public ActivityRecordEx getActivityRecordEx() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.mActivityRecord == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mAppWindowToken.mActivityRecord);
        return activityRecordEx;
    }

    public Rect getRequestedOverrideBounds() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.getRequestedOverrideBounds();
        }
        return new Rect();
    }

    public TaskEx getTaskEx() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.getTask() == null) {
            return null;
        }
        TaskEx taskEx = new TaskEx();
        taskEx.setTask(this.mAppWindowToken.getTask());
        return taskEx;
    }

    public boolean isExiting() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.mIsExiting;
        }
        return false;
    }

    public void setIsExiting(boolean mIsExiting) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.mIsExiting = mIsExiting;
        }
    }

    public boolean isClientHidden() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.isClientHidden();
        }
        return false;
    }

    public void setClientHidden(boolean clientHidden) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.setClientHidden(clientHidden);
        }
    }

    public boolean isHiddenRequested() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.hiddenRequested;
        }
        return false;
    }

    public void setHiddenRequested(boolean hiddenRequested) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.hiddenRequested = hiddenRequested;
        }
    }

    public int getLayer() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.getLayer();
        }
        return 0;
    }

    public boolean isAllDrawn() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.allDrawn;
        }
        return false;
    }

    public void setAllDrawn(boolean allDrawn) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.allDrawn = allDrawn;
        }
    }

    public boolean isRelaunching() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.isRelaunching();
        }
        return false;
    }

    public boolean isStartingDisplayed() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.startingDisplayed;
        }
        return false;
    }

    public void setStartingDisplayed(boolean startingDisplayed) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.startingDisplayed = startingDisplayed;
        }
    }

    public boolean isStartingMoved() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            return appWindowToken.startingMoved;
        }
        return false;
    }

    public void setStartingMoved(boolean startingMoved) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.startingMoved = startingMoved;
        }
    }

    public DisplayContentEx getDisplayContentEx() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.mDisplayContent == null) {
            return null;
        }
        DisplayContentEx displayContentEx = new DisplayContentEx();
        displayContentEx.setDisplayContent(this.mAppWindowToken.mDisplayContent);
        return displayContentEx;
    }

    public boolean isAppWindowTokenNull() {
        return this.mAppWindowToken == null;
    }

    public boolean isHwActivityRecord() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.mActivityRecord == null) {
            return false;
        }
        return this.mAppWindowToken.mActivityRecord instanceof ActivityRecordBridge;
    }

    public boolean inHwMagicWindowingMode() {
        if (isAppWindowTokenNull()) {
            return false;
        }
        return this.mAppWindowToken.inHwMagicWindowingMode();
    }

    public boolean getEnteringAnimation() {
        if (isAppWindowTokenNull()) {
            return false;
        }
        return this.mAppWindowToken.mEnteringAnimation;
    }

    public int getTransit() {
        if (isAppWindowTokenNull()) {
            return 0;
        }
        return this.mAppWindowToken.getTransit();
    }

    public boolean isHwActivityAniRunningBelow() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null || appWindowToken.mActivityRecord == null) {
            return false;
        }
        Object obj = this.mAppWindowToken.mActivityRecord;
        if (obj instanceof ActivityRecordBridge) {
            return ((ActivityRecordBridge) obj).isAniRunningBelow();
        }
        return false;
    }

    public String getAppPackageName() {
        if (isAppWindowTokenNull()) {
            return "";
        }
        return this.mAppWindowToken.appPackageName;
    }

    public Rect getBounds() {
        if (isAppWindowTokenNull()) {
            return new Rect();
        }
        return this.mAppWindowToken.getBounds();
    }
}
