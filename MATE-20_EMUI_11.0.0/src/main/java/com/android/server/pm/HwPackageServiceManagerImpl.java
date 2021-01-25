package com.android.server.pm;

public class HwPackageServiceManagerImpl implements HwPackageServiceManager {
    private static HwPackageServiceManagerImpl sInstance = new HwPackageServiceManagerImpl();

    public static HwPackageServiceManager getDefault() {
        return sInstance;
    }

    public void addHwSharedUserLP(Object settings) {
    }
}
