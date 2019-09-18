package com.huawei.nb.coordinator.helper;

import java.lang.Thread;

final /* synthetic */ class CoordinatorRequest$$Lambda$1 implements Thread.UncaughtExceptionHandler {
    private final CoordinatorRequest arg$1;

    CoordinatorRequest$$Lambda$1(CoordinatorRequest coordinatorRequest) {
        this.arg$1 = coordinatorRequest;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        this.arg$1.bridge$lambda$1$CoordinatorRequest(thread, th);
    }
}
