package com.huawei.nb.client.callback;

public interface WaitableCallback<T> {
    T await(int i, long j);

    void interrupt();
}
