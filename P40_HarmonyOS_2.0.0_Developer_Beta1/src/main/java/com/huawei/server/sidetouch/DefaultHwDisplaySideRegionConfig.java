package com.huawei.server.sidetouch;

public class DefaultHwDisplaySideRegionConfig {
    private static DefaultHwDisplaySideRegionConfig sInstance = null;

    public static synchronized DefaultHwDisplaySideRegionConfig getInstance() {
        DefaultHwDisplaySideRegionConfig defaultHwDisplaySideRegionConfig;
        synchronized (DefaultHwDisplaySideRegionConfig.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwDisplaySideRegionConfig();
            }
            defaultHwDisplaySideRegionConfig = sInstance;
        }
        return defaultHwDisplaySideRegionConfig;
    }

    public void updateExtendApp(String packageName, boolean isExtend) {
    }

    public boolean isExtendApp(String packageName) {
        return false;
    }

    public boolean isAppInWhiteList(String packageName) {
        return false;
    }

    public String getAppVersionInWhiteList(String packageName) {
        return null;
    }

    public void updateWhitelistByOuc() {
    }

    public int compareVersion(String version1, String version2) {
        return 0;
    }
}
