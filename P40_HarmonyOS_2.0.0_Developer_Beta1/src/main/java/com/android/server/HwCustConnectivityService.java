package com.android.server;

import android.content.Context;

public class HwCustConnectivityService {
    public boolean mMobileDataAlwaysOnCust = false;

    public boolean isSupportWifiConnectMode(Context context) {
        return false;
    }

    public boolean isNeedCustTethering(Context context) {
        return false;
    }
}
