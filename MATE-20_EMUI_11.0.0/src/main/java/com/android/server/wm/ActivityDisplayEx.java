package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.res.Configuration;
import android.view.Display;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityDisplayEx {
    private ActivityDisplay mActivityDisplay;

    public ActivityDisplayEx() {
    }

    public ActivityDisplayEx(ActivityDisplay activityDisplay) {
        this.mActivityDisplay = activityDisplay;
    }

    public ActivityDisplay getActivityDisplay() {
        return this.mActivityDisplay;
    }

    public void setActivityDisplay(ActivityDisplay activityDisplay) {
        this.mActivityDisplay = activityDisplay;
    }

    public int getChildCount() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null) {
            return 0;
        }
        return activityDisplay.getChildCount();
    }

    public ActivityStackEx getChildAt(int index) {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null || activityDisplay.getChildAt(index) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getChildAt(index));
        return activityStackEx;
    }

    public ActivityStackEx getTopStackEx() {
        if (this.mActivityDisplay == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getTopStack());
        return activityStackEx;
    }

    public ActivityStackEx getOrCreateStackEx(ActivityRecordEx activityRecordEx, ActivityOptions options, TaskRecordEx candidateTask, int activityType, boolean isOnTop) {
        ActivityRecord activityRecord = null;
        if (this.mActivityDisplay == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityRecordEx != null) {
            activityRecord = activityRecordEx.getActivityRecord();
        }
        activityStackEx.setActivityStack(activityDisplay.getOrCreateStack(activityRecord, options, candidateTask.getTaskRecord(), activityType, isOnTop));
        return activityStackEx;
    }

    public ActivityStackEx getFocusedStackEx() {
        if (this.mActivityDisplay == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getFocusedStack());
        return activityStackEx;
    }

    public Configuration getConfiguration() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay != null) {
            return activityDisplay.getConfiguration();
        }
        return null;
    }

    public ArrayList<ActivityStackEx> getStackExs() {
        if (this.mActivityDisplay == null) {
            return null;
        }
        ArrayList<ActivityStackEx> stackExs = new ArrayList<>();
        Iterator it = this.mActivityDisplay.mStacks.iterator();
        while (it.hasNext()) {
            ActivityStackEx activityStackEx = new ActivityStackEx();
            activityStackEx.setActivityStack((ActivityStack) it.next());
            stackExs.add(activityStackEx);
        }
        return stackExs;
    }

    public ActivityStackEx getStackEx(int stackId) {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null || activityDisplay.getStack(stackId) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getStack(stackId));
        return activityStackEx;
    }

    public ActivityStackEx getStackEx(int windowingMode, int activityType) {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null || activityDisplay.getStack(windowingMode, activityType) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getStack(windowingMode, activityType));
        return activityStackEx;
    }

    public ActivityStackEx getHomeStackEx() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null || activityDisplay.getHomeStack() == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityDisplay.getHomeStack());
        return activityStackEx;
    }

    public void moveStackBehindStack(ActivityStackEx mainStack, ActivityStackEx homeStack) {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay != null && mainStack != null && homeStack != null) {
            activityDisplay.moveStackBehindStack(mainStack.getActivityStack(), homeStack.getActivityStack());
        }
    }

    public int getIndexOf(ActivityStackEx stackEx) {
        if (this.mActivityDisplay == null || stackEx == null || stackEx.getActivityStack() == null) {
            return -1;
        }
        return this.mActivityDisplay.getIndexOf(stackEx.getActivityStack());
    }

    public ActivityStackEx getNextFocusableStack(ActivityStackEx currentFocus, boolean isIgnoreCurrent) {
        ActivityStack next;
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null || currentFocus == null || (next = activityDisplay.getNextFocusableStack(currentFocus.getActivityStack(), isIgnoreCurrent)) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(next);
        return activityStackEx;
    }

    public Display getDisplay() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null) {
            return null;
        }
        return activityDisplay.mDisplay;
    }

    public ActivityStackEx getTopStackInWindowingMode(int windowingMode) {
        ActivityStack topOtherSplitStack = this.mActivityDisplay.getTopStackInWindowingMode(windowingMode);
        if (topOtherSplitStack == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(topOtherSplitStack);
        return activityStackEx;
    }

    public int getDisplayId() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null) {
            return 0;
        }
        return activityDisplay.mDisplayId;
    }

    public boolean isSleeping() {
        ActivityDisplay activityDisplay = this.mActivityDisplay;
        if (activityDisplay == null) {
            return false;
        }
        return activityDisplay.isSleeping();
    }

    public boolean isActivityDisplayEmpty() {
        return this.mActivityDisplay == null;
    }
}
