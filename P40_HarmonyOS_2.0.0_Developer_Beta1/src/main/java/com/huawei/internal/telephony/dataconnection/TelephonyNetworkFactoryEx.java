package com.huawei.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;

public class TelephonyNetworkFactoryEx {
    TelephonyNetworkFactory mTelephonyNetworkFactory;

    public void setTelephonyNetworkFactory(TelephonyNetworkFactory telephonyNetworkFactory) {
        this.mTelephonyNetworkFactory = telephonyNetworkFactory;
    }

    public DcTrackerEx getDcTracker() {
        if (this.mTelephonyNetworkFactory == null) {
            return null;
        }
        DcTrackerEx dcTrackerEx = new DcTrackerEx();
        dcTrackerEx.setDcTracker(this.mTelephonyNetworkFactory.getDcTracker());
        return dcTrackerEx;
    }
}
