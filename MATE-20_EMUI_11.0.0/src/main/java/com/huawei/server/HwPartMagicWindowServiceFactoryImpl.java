package com.huawei.server;

import android.content.Context;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.HwMagicWinCombineManagerImpl;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.server.magicwin.DefaultHwMagicWinCombineManager;
import com.huawei.server.magicwin.DefaultHwMagicWinManager;
import com.huawei.server.magicwin.DefaultHwMagicWindowManagerService;
import com.huawei.server.magicwin.HwMagicWindowManagerService;

public class HwPartMagicWindowServiceFactoryImpl extends DefaultHwPartMagicWindowServiceFactoryImpl {
    private static final String TAG = "HWMW_HwPartMagicWindowServiceFactoryImpl";

    public DefaultHwMagicWindowManagerService getHwMagicWindowService(Context context, ActivityManagerServiceEx amsEx, WindowManagerServiceEx wmsEx) {
        return new HwMagicWindowManagerService(context, amsEx, wmsEx);
    }

    public DefaultHwMagicWinCombineManager getHwMagicWinCombineManager() {
        return HwMagicWinCombineManagerImpl.getInstance();
    }

    public DefaultHwMagicWinManager getHwMagicWinManager() {
        return HwMagicWinManager.getInstance();
    }
}
