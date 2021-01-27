package com.android.server.wm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Iterator;

public class TaskRecordEx {
    public static final int REPARENT_KEEP_STACK_AT_FRONT = 1;
    public static final int REPARENT_MOVE_STACK_TO_FRONT = 0;
    private static final int WINDOW_FULLSCREEN = 4;
    private ActivityTaskManagerServiceEx mAtmsEx;
    private TaskRecord mTaskRecord;

    public void setTaskRecord(TaskRecord taskRecord) {
        this.mTaskRecord = taskRecord;
    }

    public TaskRecord getTaskRecord() {
        return this.mTaskRecord;
    }

    public void resetTaskRecord(Object taskRecord) {
        if (taskRecord != null && (taskRecord instanceof TaskRecord)) {
            this.mTaskRecord = (TaskRecord) taskRecord;
        }
    }

    public int getWindowState() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 4;
        }
        return taskRecord.mWindowState;
    }

    public boolean inHwMagicWindowingMode() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.inHwMagicWindowingMode();
        }
        return false;
    }

    public int getTaskId() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 0;
        }
        return taskRecord.taskId;
    }

    public boolean isTopActivityEmpty() {
        TaskRecord taskRecord = this.mTaskRecord;
        return taskRecord == null || taskRecord.getTopActivity() == null;
    }

    public void setNextWindowState(int state) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.mNextWindowState = state;
        }
    }

    public int getNextWindowState() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 0;
        }
        return taskRecord.mNextWindowState;
    }

    public int getHwTaskRecordWindowState() {
        if (instanceOfHwTaskRecord()) {
            return this.mTaskRecord.getWindowState();
        }
        return 0;
    }

    public Rect getRequestedOverrideBounds() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return null;
        }
        return taskRecord.getRequestedOverrideBounds();
    }

    public boolean instanceOfHwTaskRecord() {
        TaskRecord taskRecord = this.mTaskRecord;
        return taskRecord != null && (taskRecord instanceof TaskRecordBridge);
    }

    public void setSaveBounds(boolean isSaveBounds) {
        if (instanceOfHwTaskRecord()) {
            this.mTaskRecord.setSaveBounds(isSaveBounds);
        }
    }

    public boolean isSaveBounds() {
        if (instanceOfHwTaskRecord()) {
            return this.mTaskRecord.isSaveBounds();
        }
        return false;
    }

    public boolean resize(Rect bounds, int resizeMode, boolean isPreserveWindow, boolean isDeferResume) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return false;
        }
        return taskRecord.resize(bounds, resizeMode, isPreserveWindow, isDeferResume);
    }

    public ActivityInfo getRootActivityInfo() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return null;
        }
        return taskRecord.mRootActivityInfo;
    }

    public boolean isRootActivityEmpty() {
        TaskRecord taskRecord = this.mTaskRecord;
        return taskRecord == null || taskRecord.getRootActivity() == null;
    }

    public String getPkgNameFromRootActivity() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.getRootActivity() == null) {
            return "";
        }
        return this.mTaskRecord.getRootActivity().packageName;
    }

    public String getPkgNameFromRootActivityInfo() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.mRootActivityInfo == null) {
            return "";
        }
        return this.mTaskRecord.mRootActivityInfo.packageName;
    }

    public int getResizeMode() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 0;
        }
        return taskRecord.mResizeMode;
    }

    public int getOriginalWindowState() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 4;
        }
        return taskRecord.mOriginalWindowState;
    }

    public void setOriginalWindowState(int state) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.mOriginalWindowState = state;
        }
    }

    public int getStackId() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return 0;
        }
        return taskRecord.getStackId();
    }

    public String getPkgNameFromTopActivity() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.getTopActivity() == null) {
            return "";
        }
        return this.mTaskRecord.getTopActivity().packageName;
    }

    public boolean isEmpty() {
        return this.mTaskRecord == null;
    }

    public boolean isVisible() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return false;
        }
        return taskRecord.isVisible();
    }

    public int getDisplayChildCount() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.mStack == null || this.mTaskRecord.mStack.getDisplay() == null) {
            return 0;
        }
        return this.mTaskRecord.mStack.getDisplay().getChildCount();
    }

    public ActivityStackEx getActivityStackExByIndex(int index) {
        ActivityStack activityStack;
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.mStack == null || this.mTaskRecord.mStack.getDisplay() == null || (activityStack = this.mTaskRecord.mStack.getDisplay().getChildAt(index)) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(activityStack);
        return activityStackEx;
    }

    public void setWindowingMode(int windowingMode) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.setWindowingMode(windowingMode);
        }
    }

    public ActivityStackEx getStack() {
        if (this.mTaskRecord == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mTaskRecord.getStack());
        return activityStackEx;
    }

    public ActivityRecordEx getChildAt(int index) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.getChildAt(index) == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mTaskRecord.getChildAt(index));
        return activityRecordEx;
    }

    public int getChildCount() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.getChildCount();
        }
        return 0;
    }

    public void moveActivityToFrontLocked(ActivityRecordEx activityRecordEx) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null && activityRecordEx != null) {
            taskRecord.moveActivityToFrontLocked(activityRecordEx.getActivityRecord());
        }
    }

    public ActivityRecordEx getRootActivity() {
        if (this.mTaskRecord == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mTaskRecord.getRootActivity());
        return activityRecordEx;
    }

    public ActivityRecordEx getTopActivity() {
        if (this.mTaskRecord == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mTaskRecord.getTopActivity());
        return activityRecordEx;
    }

    public int getActivityType() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.getActivityType();
        }
        return 0;
    }

    public void updateEffectiveIntent() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.updateEffectiveIntent();
        }
    }

    public void setFrontOfTask() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.setFrontOfTask();
        }
    }

    public TaskEx getTaskEx() {
        if (this.mTaskRecord == null) {
            return null;
        }
        TaskEx taskEx = new TaskEx();
        taskEx.setTask(this.mTaskRecord.mTask);
        return taskEx;
    }

    public boolean equalsStackId(ActivityStackEx activityStackEx) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || activityStackEx == null || taskRecord.getStackId() != activityStackEx.getStackId()) {
            return false;
        }
        return true;
    }

    public boolean equalsTaskRecord(TaskRecordEx taskRecordEx) {
        if (taskRecordEx != null) {
            if (taskRecordEx.getTaskRecord() == this.mTaskRecord) {
                return true;
            }
        } else if (this.mTaskRecord == null) {
            return true;
        }
        return false;
    }

    public int getUserId() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.userId;
        }
        return 0;
    }

    public void setUserId(int userId) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.userId = userId;
        }
    }

    public String getAffinity() {
        TaskRecord taskRecord = this.mTaskRecord;
        return taskRecord != null ? taskRecord.affinity : "";
    }

    public void setAffinity(String affinity) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.affinity = affinity;
        }
    }

    public boolean inRecents() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.inRecents;
        }
        return false;
    }

    public void setInRecents(boolean isInRecents) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.inRecents = isInRecents;
        }
    }

    public ComponentName getRealActivity() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.realActivity;
        }
        return null;
    }

    public void setRealActivity(ComponentName realActivity) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.realActivity = realActivity;
        }
    }

    public ComponentName getOrigActivity() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.origActivity;
        }
        return null;
    }

    public void setOrigActivity(ComponentName origActivity) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.origActivity = origActivity;
        }
    }

    public ArrayList<ActivityRecordEx> getActivityRecordExs() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null) {
            return null;
        }
        ArrayList<ActivityRecord> mArs = taskRecord.mActivities;
        ArrayList<ActivityRecordEx> mArExs = new ArrayList<>();
        Iterator<ActivityRecord> it = mArs.iterator();
        while (it.hasNext()) {
            ActivityRecordEx arEx = new ActivityRecordEx();
            arEx.setActivityRecord(it.next());
            mArExs.add(arEx);
        }
        return mArExs;
    }

    public void reparent(ActivityStackEx preferredStack, boolean isToTop, int moveStackMode, boolean isAnimate, boolean isDeferResume, boolean isSchedulePipModeChange, String reason) {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.reparent(preferredStack.getActivityStack(), isToTop, moveStackMode, isAnimate, isDeferResume, isSchedulePipModeChange, reason);
        }
    }

    public ActivityTaskManagerServiceEx getActivityTaskManagerServiceEx() {
        TaskRecord taskRecord;
        if (!(this.mAtmsEx != null || (taskRecord = this.mTaskRecord) == null || taskRecord.mService == null)) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mTaskRecord.mService);
        }
        return this.mAtmsEx;
    }

    public Rect getBounds() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.getBounds();
        }
        return new Rect();
    }

    public boolean removeActivity(ActivityRecordEx ar) {
        if (this.mTaskRecord == null || ar == null || ar.getActivityRecord() == null) {
            return false;
        }
        return this.mTaskRecord.removeActivity(ar.getActivityRecord());
    }

    public void addActivityAtIndex(int index, ActivityRecordEx ar) {
        if (this.mTaskRecord != null && ar != null && ar.getActivityRecord() != null) {
            this.mTaskRecord.addActivityAtIndex(index, ar.getActivityRecord());
        }
    }

    public ActivityStackEx getActivityStack() {
        ActivityStackEx activityStackEx = new ActivityStackEx();
        TaskRecord taskRecord = this.mTaskRecord;
        if (!(taskRecord == null || taskRecord.mStack == null)) {
            activityStackEx.setActivityStack(this.mTaskRecord.mStack);
        }
        return activityStackEx;
    }

    public static boolean isSameTaskRecord(TaskRecordEx sourceTask, TaskRecordEx desTask) {
        if (sourceTask == null && desTask == null) {
            return true;
        }
        if ((sourceTask == null && desTask != null) || (sourceTask != null && desTask == null)) {
            return false;
        }
        if (sourceTask.getTaskRecord() == desTask.getTaskRecord()) {
            return true;
        }
        return false;
    }

    public Intent getIntent() {
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            return taskRecord.intent;
        }
        return null;
    }

    public void setDragFullMode(int mode) {
        TaskRecordBridge taskRecordBridge = this.mTaskRecord;
        if (taskRecordBridge != null && (taskRecordBridge instanceof TaskRecordBridge)) {
            taskRecordBridge.setDragFullMode(mode);
        }
    }

    public int getDragFullMode() {
        TaskRecordBridge taskRecordBridge = this.mTaskRecord;
        if (taskRecordBridge == null || !(taskRecordBridge instanceof TaskRecordBridge)) {
            return 0;
        }
        return taskRecordBridge.getDragFullMode();
    }

    public String toString() {
        TaskRecord task = getTaskRecord();
        return task != null ? task.toString() : "null";
    }

    public Bundle getMagicWindowExtras() {
        TaskRecordBridge taskRecordBridge = this.mTaskRecord;
        if (taskRecordBridge == null || !(taskRecordBridge instanceof TaskRecordBridge)) {
            return null;
        }
        return taskRecordBridge.getMagicWindowExtras();
    }

    public void setMagicWindowExtras(Bundle bundle) {
        TaskRecordBridge taskRecordBridge = this.mTaskRecord;
        if (taskRecordBridge != null && (taskRecordBridge instanceof TaskRecordBridge)) {
            taskRecordBridge.setMagicWindowExtras(bundle);
        }
    }
}
