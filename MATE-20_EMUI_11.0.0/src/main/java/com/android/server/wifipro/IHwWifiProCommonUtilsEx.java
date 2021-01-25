package com.android.server.wifipro;

import android.content.Context;

public interface IHwWifiProCommonUtilsEx {
    boolean hwIsAllowWifiConfigRecovery(String str);

    boolean hwIsWifiProSwitchOn(Context context);
}
