package com.huawei.android.feature.install;

import java.util.concurrent.atomic.AtomicReference;

public final class FetchFeatureReference {
    private static final AtomicReference<IFetchFeature> sReference = new AtomicReference<>();

    public static IFetchFeature get() {
        return sReference.get();
    }

    public static void set(IFetchFeature iFetchFeature) {
        sReference.compareAndSet(null, iFetchFeature);
    }
}
