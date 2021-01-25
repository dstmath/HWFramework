package com.huawei.android.feature.tasks;

public class TaskHolder<TResult> {
    private final w<TResult> mTask = new w<>();

    public final Task<TResult> getTask() {
        return this.mTask;
    }

    public final boolean notifyException(Exception exc) {
        return this.mTask.notifyException(exc);
    }

    public final boolean notifyResult(TResult tresult) {
        return this.mTask.notifyResult(tresult);
    }
}
