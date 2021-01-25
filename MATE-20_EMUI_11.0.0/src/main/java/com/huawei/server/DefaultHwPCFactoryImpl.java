package com.huawei.server;

import android.content.Context;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.DefaultHwHiCarMultiWindowManager;
import com.android.server.wm.DefaultHwPCMultiWindowManager;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.hicar.DefaultHiCarManager;
import com.huawei.server.hwmultidisplay.windows.DefaultHwWindowsCastManager;
import com.huawei.server.pc.DefaultHwPCDataReporter;
import com.huawei.server.pc.DefaultHwPCManagerService;

public class DefaultHwPCFactoryImpl {
    private static DefaultHwPCFactoryImpl instance = null;

    public static synchronized DefaultHwPCFactoryImpl getInstance() {
        DefaultHwPCFactoryImpl defaultHwPCFactoryImpl;
        synchronized (DefaultHwPCFactoryImpl.class) {
            if (instance == null) {
                instance = new DefaultHwPCFactoryImpl();
            }
            defaultHwPCFactoryImpl = instance;
        }
        return defaultHwPCFactoryImpl;
    }

    public DefaultHwPCDataReporter getHwPCDataReporter() {
        return new DefaultHwPCDataReporter();
    }

    public DefaultHiCarManager getHiCarManager() {
        return new DefaultHiCarManager();
    }

    public DefaultHwMultiDisplayUtils getHwMultiDisplayUtils() {
        return new DefaultHwMultiDisplayUtils();
    }

    public DefaultHwWindowsCastManager getHwWindowsCastManager() {
        return new DefaultHwWindowsCastManager();
    }

    public DefaultHwHiCarMultiWindowManager getHwHiCarMultiWindowManager() {
        return new DefaultHwHiCarMultiWindowManager();
    }

    public DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx serviceEx) {
        return new DefaultHwPCMultiWindowManager();
    }

    public DefaultHwPCManagerService getHwPCManagerService(Context context, ActivityManagerServiceEx amsEx) {
        return new DefaultHwPCManagerService(context, amsEx);
    }
}
