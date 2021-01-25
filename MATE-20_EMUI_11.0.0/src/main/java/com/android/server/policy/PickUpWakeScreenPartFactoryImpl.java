package com.android.server.policy;

import com.huawei.server.DefaultHwBasicPlatformPartFactory;

public class PickUpWakeScreenPartFactoryImpl extends DefaultHwBasicPlatformPartFactory {
    public PickUpWakeScreenManager getPickUpWakeScreenManager() {
        return PickUpWakeScreenManager.getInstance();
    }
}
