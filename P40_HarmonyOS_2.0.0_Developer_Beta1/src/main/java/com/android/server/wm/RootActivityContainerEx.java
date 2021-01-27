package com.android.server.wm;

public class RootActivityContainerEx {
    public static final int MATCH_TASK_IN_STACKS_ONLY = 0;
    private RootActivityContainer mRootActivityContainer;

    public RootActivityContainer getRootActivityContainer() {
        return this.mRootActivityContainer;
    }

    public void setRootActivityContainer(RootActivityContainer rootActivityContainer) {
        this.mRootActivityContainer = rootActivityContainer;
    }

    public void resumeFocusedStacksTopActivities() {
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer != null) {
            rootActivityContainer.resumeFocusedStacksTopActivities();
        }
    }

    public ActivityDisplayEx getDefaultDisplay() {
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null || rootActivityContainer.getDefaultDisplay() == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(this.mRootActivityContainer.getDefaultDisplay());
        return activityDisplayEx;
    }

    public TaskRecordEx anyTaskForId(int id, int matchMode) {
        TaskRecord taskRecord;
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null || (taskRecord = rootActivityContainer.anyTaskForId(id, matchMode)) == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    public TaskRecordEx anyTaskForId(int id) {
        TaskRecord taskRecord;
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null || (taskRecord = rootActivityContainer.anyTaskForId(id)) == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    public ActivityStackEx getTopDisplayFocusedStack() {
        ActivityStack activityStack;
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null || (activityStack = rootActivityContainer.getTopDisplayFocusedStack()) == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(activityStack);
        return activityStackEx;
    }

    public int getDefaultMinSizeOfResizeableTaskDp() {
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null) {
            return 0;
        }
        return rootActivityContainer.mDefaultMinSizeOfResizeableTaskDp;
    }

    public int getChildCount() {
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null) {
            return 0;
        }
        return rootActivityContainer.getChildCount();
    }

    public ActivityDisplayEx getChildAt(int index) {
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (!(rootActivityContainer == null || rootActivityContainer.getChildAt(index) == null)) {
            activityDisplayEx.setActivityDisplay(this.mRootActivityContainer.getChildAt(index));
        }
        return activityDisplayEx;
    }

    public boolean isAppInLockList(String pgkName, int userId) {
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer == null || rootActivityContainer.getHwRootActivityContainerEx() == null) {
            return false;
        }
        return this.mRootActivityContainer.getHwRootActivityContainerEx().isAppInLockList(pgkName, userId);
    }
}
