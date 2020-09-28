package com.huawei.featurelayer.featureframework;

public interface IFeatureFramework extends IFeature {
    public static final String PACKAGE = "com.huawei.featurelayer.featureframework";

    String getLibraryPath(String str, String str2);

    boolean isFeatureExist(String str);

    IFeature loadFeature(String str, String str2);

    int releaseFeature(IFeature iFeature);
}
