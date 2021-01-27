package com.huawei.featurelayer.featureframework;

import android.content.Context;

public abstract class IFeatureEntry {
    protected final IFeatureFramework mFeatureFramework;
    protected final Context mHostContext;

    public abstract IFeatureInfo getFeatureInfo();

    public abstract IFeature loadFeature(String str, String str2);

    public abstract int releaseFeature(IFeature iFeature);

    public IFeatureEntry(Context hostContext, IFeatureFramework featureFramework) {
        this.mHostContext = hostContext;
        this.mFeatureFramework = featureFramework;
    }
}
