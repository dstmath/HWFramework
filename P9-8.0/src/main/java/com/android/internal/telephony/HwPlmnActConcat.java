package com.android.internal.telephony;

import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class HwPlmnActConcat {
    private static final String TAG = "HwPlmnActConcat";
    private static boolean mNeedConcat = HuaweiTelephonyConfigs.isChinaMobile();

    public static boolean needPlmnActConcat() {
        return mNeedConcat;
    }

    public static String getPlmnActConcat(String plmnValue, ServiceState ss) {
        if (TextUtils.isEmpty(plmnValue)) {
            return null;
        }
        int voiceRegState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        int voiceNetworkType = ss.getVoiceNetworkType();
        int dataNetworkType = ss.getDataNetworkType();
        String plmnActValue = plmnValue;
        int networkType = 0;
        if (dataRegState == 0) {
            networkType = dataNetworkType;
        } else if (voiceRegState == 0) {
            networkType = voiceNetworkType;
        }
        Rlog.d(TAG, "plmnValue:" + plmnValue + ",voiceNetworkType:" + voiceNetworkType + ",dataNetworkType:" + dataNetworkType + ",NetworkType:" + networkType);
        String act = null;
        switch (TelephonyManager.getNetworkClass(networkType)) {
            case 1:
                act = "";
                break;
            case 2:
                act = "3G";
                break;
            case 3:
                act = "4G";
                break;
            default:
                Rlog.d(TAG, "network class unknow");
                break;
        }
        if (!(act == null || (plmnValue.endsWith(act) ^ 1) == 0)) {
            plmnActValue = plmnValue + act;
        }
        Rlog.d(TAG, "plmnActValue:" + plmnActValue);
        return plmnActValue;
    }
}
