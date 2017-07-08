package com.android.server.wifi;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.server.HwCustConnectivityService;
import huawei.cust.HwCustUtils;

public class HwWifiSettingsStoreEx {
    static final String WIFI_CONNECT_TYPE = "wifi_connect_type";
    private static final int WIFI_CONNECT_TYPE_AUTO = 0;
    private Context mContext;
    private HwCustConnectivityService mCust;
    private boolean mIsAutoConnectionEnabled;

    public HwWifiSettingsStoreEx(Context context) {
        this.mCust = (HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]);
        this.mContext = context;
        this.mIsAutoConnectionEnabled = getPersistedAutoConnect();
    }

    synchronized boolean isAutoConnectionEnabled() {
        return this.mIsAutoConnectionEnabled;
    }

    synchronized void handleWifiAutoConnectChanged() {
        this.mIsAutoConnectionEnabled = getPersistedAutoConnect();
    }

    private boolean getPersistedAutoConnect() {
        if (("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) || this.mCust.isSupportWifiConnectMode()) && System.getInt(this.mContext.getContentResolver(), WIFI_CONNECT_TYPE, 0) != 0) {
            return false;
        }
        return true;
    }
}
