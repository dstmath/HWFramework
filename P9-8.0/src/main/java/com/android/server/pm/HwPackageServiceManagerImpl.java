package com.android.server.pm;

public class HwPackageServiceManagerImpl implements HwPackageServiceManager {
    private static HwPackageServiceManagerImpl mInstance = new HwPackageServiceManagerImpl();

    public static HwPackageServiceManager getDefault() {
        return mInstance;
    }

    public void addHwSharedUserLP(Object settings) {
        SettingsUtils.addSharedUserLPw(settings, "org.simalliance.uid.openmobileapi", 1091, 1, 1);
    }
}
