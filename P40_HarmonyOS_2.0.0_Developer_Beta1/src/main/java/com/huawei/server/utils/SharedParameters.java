package com.huawei.server.utils;

import android.content.Context;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.WindowManagerServiceEx;

public class SharedParameters {
    private ActivityManagerServiceEx mAms;
    private Context mContext;
    private HwMagicWinManager mHwMagicWinManager;
    private WindowManagerServiceEx mWms;

    public SharedParameters(HwMagicWinManager manager, Context context, ActivityManagerServiceEx ams, WindowManagerServiceEx wms) {
        this.mHwMagicWinManager = manager;
        this.mContext = context;
        this.mAms = ams;
        this.mWms = wms;
    }

    public HwMagicWinManager getMwWinManager() {
        return this.mHwMagicWinManager;
    }

    public Context getContext() {
        return this.mContext;
    }

    public ActivityManagerServiceEx getAms() {
        return this.mAms;
    }

    public WindowManagerServiceEx getWms() {
        return this.mWms;
    }
}
