package com.huawei.android.feature.tasks;

public interface OnCompleteListener<TResult> {
    void onComplete(Task<TResult> task);
}
