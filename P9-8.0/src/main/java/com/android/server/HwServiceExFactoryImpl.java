package com.android.server;

import android.content.Context;
import com.android.server.HwServiceExFactory.Factory;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.am.HwActivityStarterEx;
import com.android.server.am.IHwActivityManagerInner;
import com.android.server.am.IHwActivityManagerServiceEx;
import com.android.server.imm.HwInputMethodManagerServiceEx;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.pm.IHwPackageManagerInner;
import com.android.server.pm.IHwPackageManagerServiceEx;
import com.android.server.wm.HwWindowManagerServiceEx;
import com.android.server.wm.IHwWindowManagerInner;
import com.android.server.wm.IHwWindowManagerServiceEx;
import com.huawei.server.am.IHwActivityStarterEx;

public class HwServiceExFactoryImpl implements Factory {
    private static final String TAG = "HwServiceExFactoryImpl";

    public IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner ams, Context context) {
        return new HwActivityManagerServiceEx(ams, context);
    }

    public IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        return new HwWindowManagerServiceEx(wms, context);
    }

    public IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        return new HwPackageManagerServiceEx(pms, context);
    }

    public IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner ims, Context context) {
        return new HwInputMethodManagerServiceEx(ims, context);
    }

    public IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService ams) {
        return new HwActivityStarterEx(ams);
    }
}
