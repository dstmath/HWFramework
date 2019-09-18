package com.huawei.android.feature.install;

import java.util.concurrent.ThreadFactory;

public class InstallThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, "IsolatedInstall Background Install Thread");
    }
}
