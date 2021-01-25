package com.huawei.server;

import android.content.Context;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.HwHiCarMultiWindowManager;
import com.android.server.wm.HwPCMultiWindowManager;
import com.huawei.server.hwmultidisplay.HwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.hicar.HiCarManager;
import com.huawei.server.hwmultidisplay.windows.HwWindowsCastManager;
import com.huawei.server.pc.HwPCDataReporter;
import com.huawei.server.pc.HwPCManagerService;

public class HwPCFactoryImpl extends DefaultHwPCFactoryImpl {
    public HwPCDataReporter getHwPCDataReporter() {
        return HwPCDataReporter.getInstance();
    }

    public HiCarManager getHiCarManager() {
        return HiCarManager.getInstance();
    }

    public HwMultiDisplayUtils getHwMultiDisplayUtils() {
        return HwMultiDisplayUtils.getInstance();
    }

    public HwWindowsCastManager getHwWindowsCastManager() {
        return HwWindowsCastManager.getDefault();
    }

    public HwHiCarMultiWindowManager getHwHiCarMultiWindowManager() {
        return HwHiCarMultiWindowManager.getInstance();
    }

    public HwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx serviceEx) {
        return HwPCMultiWindowManager.getInstance(serviceEx);
    }

    public HwPCManagerService getHwPCManagerService(Context context, ActivityManagerServiceEx amsEx) {
        return new HwPCManagerService(context, amsEx);
    }
}
