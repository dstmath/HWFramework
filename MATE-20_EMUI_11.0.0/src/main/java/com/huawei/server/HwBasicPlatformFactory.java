package com.huawei.server;

public class HwBasicPlatformFactory {
    public static final String HW_EYE_PROTECTION_PART_FACTORY_IMPL = "com.android.server.display.HwEyeProtectionPartFactoryImpl";
    public static final String HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL = "com.huawei.server.HwPartBasicPlatformServicesFactory";
    public static final String PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL = "com.android.server.policy.PickUpWakeScreenPartFactoryImpl";
    private static final String TAG = "HwBasicPlatformFactory";

    private HwBasicPlatformFactory() {
    }

    public static DefaultHwBasicPlatformPartFactory loadFactory(String factoryName) {
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof DefaultHwBasicPlatformPartFactory)) {
            return DefaultHwBasicPlatformPartFactory.getInstance();
        }
        return (DefaultHwBasicPlatformPartFactory) object;
    }
}
