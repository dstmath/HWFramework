package com.huawei.android.feature.install;

import java.util.concurrent.ThreadFactory;

public class InstallThreadFactory implements ThreadFactory {
    @Override // java.util.concurrent.ThreadFactory
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, "IsolatedInstall Background Install Thread");
    }
}
