package com.android.server.pm;

public class HwPackageServiceManagerImpl extends DefaultHwPackageServiceManager {
    private static HwPackageServiceManagerImpl sInstance = new HwPackageServiceManagerImpl();

    public static HwPackageServiceManagerImpl getDefault() {
        return sInstance;
    }

    public void addHwSharedUserLP(Object settings) {
    }
}
