package com.android.server.wm;

public class RecentTasksEx {
    private RecentTasks mRecentTasks;

    public RecentTasks getRecentTasks() {
        return this.mRecentTasks;
    }

    public void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
    }

    public int getRawTasksSize() {
        RecentTasks recentTasks = this.mRecentTasks;
        if (recentTasks != null) {
            return recentTasks.getRawTasks().size();
        }
        return 0;
    }

    public TaskRecordEx getRawTaskOfIndex(int index) {
        RecentTasks recentTasks = this.mRecentTasks;
        if (recentTasks == null || recentTasks.getRawTasks() == null || index >= getRawTasksSize()) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord((TaskRecord) this.mRecentTasks.getRawTasks().get(index));
        return taskRecordEx;
    }

    public void removeTaskRecord(TaskRecordEx taskRecordEx) {
        if (this.mRecentTasks != null && taskRecordEx.getTaskRecord() != null) {
            this.mRecentTasks.remove(taskRecordEx.getTaskRecord());
        }
    }

    public void addTaskRecord(TaskRecordEx taskRecordEx) {
        if (this.mRecentTasks != null && taskRecordEx.getTaskRecord() != null) {
            this.mRecentTasks.add(taskRecordEx.getTaskRecord());
        }
    }
}
