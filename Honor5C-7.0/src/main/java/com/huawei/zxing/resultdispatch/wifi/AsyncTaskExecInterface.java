package com.huawei.zxing.resultdispatch.wifi;

import android.os.AsyncTask;

public interface AsyncTaskExecInterface {
    <T> void execute(AsyncTask<T, ?, ?> asyncTask, T... tArr);
}
