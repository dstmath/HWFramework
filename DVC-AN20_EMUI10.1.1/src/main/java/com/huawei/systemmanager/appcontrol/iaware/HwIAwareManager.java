package com.huawei.systemmanager.appcontrol.iaware;

public class HwIAwareManager {
    public static IMultiTaskManager getMultiTaskManager() {
        return HwMultiTaskManagerImpl.getInstance();
    }
}
