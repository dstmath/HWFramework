package com.huawei.android.app;

import com.huawei.android.os.IMWThirdpartyCallbackEx;

public class HwActivityTaskManagerAdapter {
    public static boolean unregisterThirdPartyCallBack(IMWThirdpartyCallbackEx callbackEx) {
        return HwActivityTaskManager.unregisterThirdPartyCallBack(callbackEx.getCallback());
    }

    public static boolean registerThirdPartyCallBack(IMWThirdpartyCallbackEx callbackEx) {
        return HwActivityTaskManager.registerThirdPartyCallBack(callbackEx.getCallback());
    }
}
