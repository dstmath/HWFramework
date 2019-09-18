package com.huawei.android.feature.install;

import android.util.Log;
import com.huawei.android.feature.tasks.TaskHolder;

public abstract class RemoteRequest implements Runnable {
    private static final String TAG = RemoteRequest.class.getSimpleName();
    TaskHolder<?> mTaskHolder;

    public RemoteRequest(TaskHolder<?> taskHolder) {
        this.mTaskHolder = taskHolder;
    }

    public abstract void excute();

    public TaskHolder<?> getTaskHolder() {
        return this.mTaskHolder;
    }

    public void run() {
        try {
            excute();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (this.mTaskHolder != null) {
                this.mTaskHolder.notifyException(e);
            }
        }
    }
}
