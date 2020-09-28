package com.huawei.okhttp3;

import java.util.List;

public interface iDispatcher {
    void cancelAll();

    int getMaxRequests();

    int getMaxRequestsPerHost();

    List<Call> queuedCalls();

    int queuedCallsCount();

    List<Call> runningCalls();

    int runningCallsCount();

    void setIdleCallback(Runnable runnable);

    void setMaxRequests(int i);

    void setMaxRequestsPerHost(int i);
}
