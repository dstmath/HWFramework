package com.huawei.zxing.resultdispatch.wifi;

import android.os.AsyncTask;

public final class DefaultAsyncTaskExecInterface implements AsyncTaskExecInterface {
    public <T> void execute(AsyncTask<T, ?, ?> task, T... args) {
        task.execute(args);
    }
}
