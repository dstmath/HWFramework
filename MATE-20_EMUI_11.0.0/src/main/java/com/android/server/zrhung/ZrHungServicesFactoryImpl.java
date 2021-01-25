package com.android.server.zrhung;

import android.content.Context;
import com.android.commgmt.zrhung.DefaultZrHungServicesFactory;
import com.android.server.wm.HwDisplayContentEx;
import com.android.server.wm.IHwDisplayContentEx;
import com.huawei.android.util.SlogEx;

public class ZrHungServicesFactoryImpl extends DefaultZrHungServicesFactory {
    private static final String TAG = "ZrHungServicesFactoryImpl";

    public IZRHungService getDefaultZRHungService(Context context) {
        SlogEx.i(TAG, "getZRHungService");
        return ZRHungService.getInstance();
    }

    public IHwDisplayContentEx getDefaultHwDispalyContentEx() {
        SlogEx.i(TAG, "getHwDispalyContentEx");
        return new HwDisplayContentEx();
    }
}
