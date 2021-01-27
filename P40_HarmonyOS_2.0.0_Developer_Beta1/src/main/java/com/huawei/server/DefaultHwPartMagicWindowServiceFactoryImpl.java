package com.huawei.server;

import android.content.Context;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.annotation.HwSystemApi;
import com.huawei.server.magicwin.DefaultHwMagicWinAnimation;
import com.huawei.server.magicwin.DefaultHwMagicWinCombineManager;
import com.huawei.server.magicwin.DefaultHwMagicWinManager;
import com.huawei.server.magicwin.DefaultHwMagicWindowManagerService;

@HwSystemApi
public class DefaultHwPartMagicWindowServiceFactoryImpl {
    private static DefaultHwPartMagicWindowServiceFactoryImpl sInstance = new DefaultHwPartMagicWindowServiceFactoryImpl();

    public static DefaultHwPartMagicWindowServiceFactoryImpl getInstance() {
        return sInstance;
    }

    public DefaultHwMagicWindowManagerService getHwMagicWindowService(Context context, ActivityManagerServiceEx amsEx, WindowManagerServiceEx wmsEx) {
        return new DefaultHwMagicWindowManagerService(context, amsEx, wmsEx);
    }

    public DefaultHwMagicWinCombineManager getHwMagicWinCombineManager() {
        return DefaultHwMagicWinCombineManager.getInstance();
    }

    public DefaultHwMagicWinAnimation getHwMagicWinAnimation() {
        return DefaultHwMagicWinAnimation.getInstance();
    }

    public DefaultHwMagicWinManager getHwMagicWinManager() {
        return DefaultHwMagicWinManager.getInstance();
    }
}
