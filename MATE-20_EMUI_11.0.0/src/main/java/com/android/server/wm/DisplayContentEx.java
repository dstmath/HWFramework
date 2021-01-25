package com.android.server.wm;

import android.content.res.Configuration;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import com.huawei.android.view.DisplayInfoEx;
import java.util.Iterator;

public class DisplayContentEx {
    private DisplayContent mDisplayContent;

    public DisplayContentEx() {
    }

    public DisplayContentEx(DisplayContent displayContent) {
        this.mDisplayContent = displayContent;
    }

    public void setDisplayContent(DisplayContent displayContent) {
        this.mDisplayContent = displayContent;
    }

    public DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    public DisplayRotationEx getDisplayRotationEx() {
        return new DisplayRotationEx(this.mDisplayContent.getDisplayRotation());
    }

    public DisplayPolicyEx getDisplayPolicyEx() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.getDisplayPolicy() == null) {
            return null;
        }
        DisplayPolicyEx displayPolicyEx = new DisplayPolicyEx();
        displayPolicyEx.setDisplayPolicy(this.mDisplayContent.getDisplayPolicy());
        return displayPolicyEx;
    }

    public AppWindowTokenExt getFocusedApp() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.getDisplayPolicy() == null) {
            return null;
        }
        AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
        appWindowTokenEx.setAppWindowToken(this.mDisplayContent.mFocusedApp);
        return appWindowTokenEx;
    }

    public WindowStateEx getInputMethodTarget() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.mInputMethodTarget == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(this.mDisplayContent.mInputMethodTarget);
        return windowStateEx;
    }

    public DisplayInfoEx getDisplayInfoEx() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.getDisplayInfo() == null) {
            return null;
        }
        DisplayInfoEx displayInfoEx = new DisplayInfoEx();
        displayInfoEx.setDisplayInfo(this.mDisplayContent.getDisplayInfo());
        return displayInfoEx;
    }

    public int getRotation() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null) {
            return 0;
        }
        return displayContent.getRotation();
    }

    public boolean closingAppsContains(AppWindowTokenExt awtEx) {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.mClosingApps == null) {
            return false;
        }
        return this.mDisplayContent.mClosingApps.contains(awtEx.getAppWindowToken());
    }

    public boolean openingAppsContains(AppWindowTokenExt awtEx) {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.mOpeningApps == null) {
            return false;
        }
        return this.mDisplayContent.mOpeningApps.contains(awtEx.getAppWindowToken());
    }

    public ArraySet<AppWindowTokenExt> getClosingApps() {
        ArraySet<AppWindowTokenExt> appWindowTokenExes = new ArraySet<>();
        DisplayContent displayContent = this.mDisplayContent;
        if (!(displayContent == null || displayContent.mClosingApps == null)) {
            Iterator<AppWindowToken> it = this.mDisplayContent.mClosingApps.iterator();
            while (it.hasNext()) {
                AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
                appWindowTokenEx.setAppWindowToken(it.next());
                appWindowTokenExes.add(appWindowTokenEx);
            }
        }
        return appWindowTokenExes;
    }

    public ArraySet<AppWindowTokenExt> getOpeningApps() {
        ArraySet<AppWindowTokenExt> appWindowTokenExes = new ArraySet<>();
        DisplayContent displayContent = this.mDisplayContent;
        if (!(displayContent == null || displayContent.mOpeningApps == null)) {
            Iterator<AppWindowToken> it = this.mDisplayContent.mOpeningApps.iterator();
            while (it.hasNext()) {
                AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
                appWindowTokenEx.setAppWindowToken(it.next());
                appWindowTokenExes.add(appWindowTokenEx);
            }
        }
        return appWindowTokenExes;
    }

    public AppTransitionEx getAppTransitionEx() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.mAppTransition == null) {
            return null;
        }
        AppTransitionEx appTransitionEx = new AppTransitionEx();
        appTransitionEx.setAppTransition(this.mDisplayContent.mAppTransition);
        return appTransitionEx;
    }

    public WindowContainerEx getTaskStackContainers() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null || displayContent.mTaskStackContainers == null) {
            return null;
        }
        WindowContainerEx windowContainerEx = new WindowContainerEx();
        windowContainerEx.setWindowContainer(this.mDisplayContent.mTaskStackContainers);
        return windowContainerEx;
    }

    public void pauseRotationLocked() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null && displayContent.getDisplayPolicy() != null) {
            this.mDisplayContent.pauseRotationLocked();
        }
    }

    public void resumeRotationLocked() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null && displayContent.getDisplayPolicy() != null) {
            this.mDisplayContent.resumeRotationLocked();
        }
    }

    public boolean isStackVisible(int windowingMode) {
        return this.mDisplayContent.isStackVisible(windowingMode);
    }

    public TaskStackEx getStack(int windowingMode, int activityType) {
        TaskStackEx taskStackEx = new TaskStackEx();
        taskStackEx.setTaskStack(this.mDisplayContent.getStack(windowingMode, activityType));
        return taskStackEx;
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayContent.getDisplayMetrics();
    }

    public Configuration getConfiguration() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null) {
            return displayContent.getConfiguration();
        }
        return null;
    }

    public DockedStackDividerControllerAospEx getDockedStackDividerControllerEx() {
        if (this.mDisplayContent == null) {
            return null;
        }
        DockedStackDividerControllerAospEx dockedStackDividerControllerAospEx = new DockedStackDividerControllerAospEx();
        dockedStackDividerControllerAospEx.setDockedStackDividerController(this.mDisplayContent.mDividerControllerLocked);
        return dockedStackDividerControllerAospEx;
    }

    public int getInitialDisplayWidth() {
        return this.mDisplayContent.mInitialDisplayWidth;
    }

    public int getInitialDisplayHeight() {
        return this.mDisplayContent.mInitialDisplayHeight;
    }
}
