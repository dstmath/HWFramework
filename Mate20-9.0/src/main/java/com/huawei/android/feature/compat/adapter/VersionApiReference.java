package com.huawei.android.feature.compat.adapter;

import java.util.concurrent.atomic.AtomicReference;

public class VersionApiReference {
    private static final AtomicReference<VersionApi> sReference = new AtomicReference<>();

    public static VersionApi get() {
        return sReference.get();
    }

    public static void set(VersionApi versionApi) {
        sReference.compareAndSet(null, versionApi);
    }
}
