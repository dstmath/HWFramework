package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.wm.ActivityStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityStackEx {
    private ActivityStack mActivityStack;
    private ActivityTaskManagerServiceEx mAtmsEx;

    public enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED,
        RESTARTING_PROCESS
    }

    public ActivityStackEx() {
    }

    public ActivityStackEx(ActivityStack activityStack) {
        this.mActivityStack = activityStack;
    }

    public ActivityStack getActivityStack() {
        return this.mActivityStack;
    }

    public void setActivityStack(ActivityStack activityStack) {
        this.mActivityStack = activityStack;
    }

    public void resetActivityStack(Object activityStack) {
        if (activityStack instanceof ActivityStack) {
            this.mActivityStack = (ActivityStack) activityStack;
        }
    }

    public int getDisplayId() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return 0;
        }
        return activityStack.mDisplayId;
    }

    public int getChildCount() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return 0;
        }
        return activityStack.getChildCount();
    }

    public boolean shouldBeVisible(ActivityRecordEx activityRecordEx) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return false;
        }
        return activityStack.shouldBeVisible(activityRecordEx == null ? null : activityRecordEx.getActivityRecord());
    }

    public TaskRecordEx getChildAt(int index) {
        if (this.mActivityStack == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(this.mActivityStack.getChildAt(index));
        return taskRecordEx;
    }

    public TaskRecordEx topTask() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.topTask() == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(this.mActivityStack.topTask());
        return taskRecordEx;
    }

    public ActivityRecordEx getResumedActivity() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.mResumedActivity == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mActivityStack.mResumedActivity);
        return activityRecordEx;
    }

    public ArrayList<TaskRecordEx> getTaskHistory() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return new ArrayList<>();
        }
        ArrayList<TaskRecord> recordArrayList = activityStack.mTaskHistory;
        ArrayList<TaskRecordEx> results = new ArrayList<>();
        Iterator<TaskRecord> it = recordArrayList.iterator();
        while (it.hasNext()) {
            TaskRecordEx taskRecordEx = new TaskRecordEx();
            taskRecordEx.setTaskRecord(it.next());
            results.add(taskRecordEx);
        }
        return results;
    }

    public ActivityRecordEx getPausingActivity() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.mPausingActivity == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mActivityStack.mPausingActivity);
        return activityRecordEx;
    }

    public void setPausingActivity(ActivityRecordEx activityRecordEx) {
        if (this.mActivityStack != null) {
            ActivityRecord ar = null;
            if (activityRecordEx != null) {
                ar = activityRecordEx.getActivityRecord();
            }
            this.mActivityStack.mPausingActivity = ar;
        }
    }

    public void onActivityStateChanged(ActivityRecordEx record, ActivityState state, String reason) {
        if (this.mActivityStack != null) {
            ActivityRecord ar = null;
            if (record != null) {
                ar = record.getActivityRecord();
            }
            ActivityStack.ActivityState result = ActivityStack.ActivityState.INITIALIZING;
            ActivityStack.ActivityState[] values = ActivityStack.ActivityState.values();
            for (ActivityStack.ActivityState activityState : values) {
                if (state.ordinal() == activityState.ordinal()) {
                    result = activityState;
                }
            }
            if (ar != null) {
                this.mActivityStack.onActivityStateChanged(ar, result, reason);
            }
        }
    }

    public boolean inSplitScreenWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inSplitScreenWindowingMode();
        }
        return false;
    }

    public void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            activityStack.resize(bounds, tempTaskBounds, tempTaskInsetBounds);
        }
    }

    public void setWindowingMode(int preferredWindowingMode, boolean isAnimate, boolean isShowRecents, boolean isEnteringSplitScreenMode, boolean isDeferEnsuringVisibility, boolean isCreating) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            activityStack.setWindowingMode(preferredWindowingMode, isAnimate, isShowRecents, isEnteringSplitScreenMode, isDeferEnsuringVisibility, isCreating);
        }
    }

    public void setWindowingMode(int windowMode) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            activityStack.setWindowingMode(windowMode);
        }
    }

    public boolean inPinnedWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inPinnedWindowingMode();
        }
        return false;
    }

    public boolean inSplitScreenPrimaryWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inSplitScreenPrimaryWindowingMode();
        }
        return false;
    }

    public boolean inHwFreeFormWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inHwFreeFormWindowingMode();
        }
        return false;
    }

    public int getActivityType() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.getActivityType();
        }
        return 0;
    }

    public boolean inHwSplitScreenWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inHwSplitScreenWindowingMode();
        }
        return false;
    }

    public boolean isFocusedStackOnDisplay() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.isFocusedStackOnDisplay();
        }
        return false;
    }

    public boolean inSplitScreenSecondaryWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inSplitScreenSecondaryWindowingMode();
        }
        return false;
    }

    public ActivityTaskManagerServiceEx getActivityTaskManagerServiceEx() {
        ActivityStack activityStack;
        if (!(this.mAtmsEx != null || (activityStack = this.mActivityStack) == null || activityStack.mService == null)) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mActivityStack.mService);
        }
        return this.mAtmsEx;
    }

    public int getStackId() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return 0;
        }
        return activityStack.getStackId();
    }

    public boolean equalsStack(ActivityStackEx stackEx) {
        if (this.mActivityStack == null) {
            return false;
        }
        ActivityStack activityStack = null;
        if (stackEx != null) {
            activityStack = stackEx.getActivityStack();
        }
        if (this.mActivityStack == activityStack) {
            return true;
        }
        return false;
    }

    public boolean isHwActivityStack() {
        if (this.mActivityStack != null) {
            return getActivityStack() instanceof ActivityStackBridge;
        }
        return false;
    }

    public ActivityRecordEx getTopActivity() {
        ActivityRecord ar;
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || (ar = activityStack.getTopActivity()) == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(ar);
        return activityRecordEx;
    }

    public Rect getRequestedOverrideBounds() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.getRequestedOverrideBounds();
        }
        return new Rect();
    }

    public int getWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.getWindowingMode();
        }
        return 0;
    }

    public boolean isTopActivityVisible() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.isTopActivityVisible();
        }
        return false;
    }

    public static boolean containsStack(List<ActivityStackEx> stackExList, ActivityStackEx asEx) {
        if (stackExList == null || stackExList.isEmpty() || asEx == null) {
            return false;
        }
        for (ActivityStackEx as : stackExList) {
            if (asEx.getActivityStack() == as.getActivityStack()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMwNewTaskSplitStack() {
        if (this.mActivityStack == null || !isHwActivityStack()) {
            return false;
        }
        return getHwActivityStack().isMwNewTaskSplitStack();
    }

    public void setMwNewTaskSplitStack(boolean isMwNewTask) {
        if (this.mActivityStack != null && isHwActivityStack()) {
            getHwActivityStack().setIsMwNewTaskSplitStack(isMwNewTask);
        }
    }

    private ActivityStackBridge getHwActivityStack() {
        if (this.mActivityStack == null || !isHwActivityStack()) {
            return null;
        }
        return this.mActivityStack;
    }

    public ArrayList<TaskRecordEx> getAllTaskRecordExs() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return new ArrayList<>();
        }
        ArrayList<TaskRecord> trList = activityStack.getAllTasks();
        ArrayList<TaskRecordEx> trExList = new ArrayList<>();
        Iterator<TaskRecord> it = trList.iterator();
        while (it.hasNext()) {
            TaskRecordEx taskRecordEx = new TaskRecordEx();
            taskRecordEx.setTaskRecord(it.next());
            trExList.add(taskRecordEx);
        }
        return trExList;
    }

    public void removeTask(TaskRecordEx taskEx, String reason, int mode) {
        if (this.mActivityStack != null) {
            TaskRecord tr = null;
            if (taskEx != null) {
                tr = taskEx.getTaskRecord();
            }
            if (tr != null) {
                this.mActivityStack.removeTask(tr, reason, mode);
            }
        }
    }

    public TaskRecordEx createTaskRecordEx(int taskId, ActivityInfo info, Intent intent, boolean isToTop) {
        TaskRecord taskRecord;
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || (taskRecord = activityStack.createTaskRecord(taskId, info, intent, (IVoiceInteractionSession) null, (IVoiceInteractor) null, isToTop)) == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    public boolean inHwMagicWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.inHwMagicWindowingMode();
        }
        return false;
    }

    public boolean isInStackLocked(TaskRecordEx taskRecordEx) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || taskRecordEx == null) {
            return false;
        }
        return activityStack.isInStackLocked(taskRecordEx.getTaskRecord());
    }

    public TaskRecordEx taskForIdLocked(int taskId) {
        TaskRecord taskRecord;
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || (taskRecord = activityStack.taskForIdLocked(taskId)) == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    public void moveToFront(String reason) {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            activityStack.moveToFront(reason);
        }
    }

    public void finishActivityLocked(ActivityRecordEx activityRecordEx, int resultCode, Intent resultData, String reason, boolean isOomAdj, boolean isPauseImmediately) {
        if (this.mActivityStack != null && activityRecordEx != null && activityRecordEx.getActivityRecord() != null) {
            this.mActivityStack.finishActivityLocked(activityRecordEx.getActivityRecord(), resultCode, resultData, reason, isOomAdj, isPauseImmediately);
        }
    }

    public void ensureActivitiesVisibleLocked(ActivityRecordEx starting, int configChanges, boolean isPreserveWindows) {
        if (this.mActivityStack != null) {
            ActivityRecord activityRecord = null;
            if (starting != null) {
                activityRecord = starting.getActivityRecord();
            }
            this.mActivityStack.ensureActivitiesVisibleLocked(activityRecord, configChanges, isPreserveWindows);
        }
    }

    public ArrayList<ActivityRecordEx> getLRUActivities() {
        ArrayList<ActivityRecordEx> results = new ArrayList<>();
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            Iterator<ActivityRecord> it = activityStack.mLRUActivities.iterator();
            while (it.hasNext()) {
                ActivityRecordEx activityRecordEx = new ActivityRecordEx();
                activityRecordEx.setActivityRecord(it.next());
                results.add(activityRecordEx);
            }
        }
        return results;
    }

    public int numActivities() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.numActivities();
        }
        return 0;
    }

    public Rect getBounds() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.getBounds();
        }
        return new Rect();
    }

    public DisplayPolicyEx getDisplayPolicyEx() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.getDisplay() == null || this.mActivityStack.getDisplay().mDisplayContent == null || this.mActivityStack.getDisplay().mDisplayContent.getDisplayPolicy() == null) {
            return null;
        }
        DisplayPolicyEx dpEx = new DisplayPolicyEx();
        dpEx.setDisplayPolicy(this.mActivityStack.getDisplay().mDisplayContent.getDisplayPolicy());
        return dpEx;
    }

    public boolean isHomeOrRecentsStack() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            return activityStack.isHomeOrRecentsStack();
        }
        return false;
    }

    public ActivityRecordEx getCurrentResumedActivity() {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.mResumedActivity == null) {
            return null;
        }
        activityRecordEx.setActivityRecord(this.mActivityStack.mResumedActivity);
        return activityRecordEx;
    }

    public boolean inFreeformWindowingMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return false;
        }
        return activityStack.inFreeformWindowingMode();
    }

    public ActivityDisplayEx getDisplay() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || activityStack.getDisplay() == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(this.mActivityStack.getDisplay());
        return activityDisplayEx;
    }

    public ActivityRecordEx topRunningActivityLocked() {
        ActivityRecord activityRecord;
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null || (activityRecord = activityStack.topRunningActivityLocked()) == null) {
            return null;
        }
        return new ActivityRecordEx(activityRecord);
    }

    public boolean inMultiWindowMode() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack == null) {
            return false;
        }
        return activityStack.inMultiWindowMode();
    }

    public boolean isActivityStackNull() {
        return this.mActivityStack == null;
    }

    public String toString() {
        ActivityStack stack = getActivityStack();
        return stack != null ? stack.toString() : "null";
    }
}
