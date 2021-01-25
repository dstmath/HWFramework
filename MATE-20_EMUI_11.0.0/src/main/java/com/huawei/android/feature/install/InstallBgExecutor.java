package com.huawei.android.feature.install;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstallBgExecutor {
    private static final Executor EXECUTOR = Executors.newSingleThreadScheduledExecutor(new InstallThreadFactory());

    public static Executor getExecutor() {
        return EXECUTOR;
    }
}
