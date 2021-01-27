package com.huawei.android.feature.compat.adapter;

import java.util.concurrent.atomic.AtomicReference;

public class VersionApiReference {
    private static final AtomicReference<VersionApi> REFERENCE = new AtomicReference<>();

    public static VersionApi get() {
        return REFERENCE.get();
    }

    public static void set(VersionApi versionApi) {
        REFERENCE.compareAndSet(null, versionApi);
    }
}
