package com.android.server.wifi.hwUtil;

import android.net.wifi.WifiConfiguration;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.HwWifiServiceFactory;

public class HwTelephonyUtilEx {
    private static final String TAG = "HwTelephonyUtilEx";
    private static int mAssignedSubId = HwWifiServiceFactory.getHwTelphonyUtils().getDefault4GSlotId();

    public static int getEapSubId(TelephonyManager tm, WifiConfiguration config) {
        if (config == null) {
            Log.d(TAG, "getEapSubId(): config is null, get subId=" + mAssignedSubId);
            return mAssignedSubId;
        }
        int subId = config.enterpriseConfig.getEapSubId();
        boolean isMultiSimEnabled = tm.isMultiSimEnabled();
        int sub1State = tm.getSimState(0);
        int sub2State = tm.getSimState(1);
        if (!isMultiSimEnabled || sub1State != 5 || sub2State != 5 || subId == Integer.MAX_VALUE) {
            subId = HwWifiServiceFactory.getHwTelphonyUtils().getDefault4GSlotId();
        }
        mAssignedSubId = subId;
        Log.d(TAG, "checkUseDefaultSubId: isMultiSimEnabled=" + isMultiSimEnabled + ", sub1State=" + sub1State + ", sub2State=" + sub2State + ", subId=" + subId + ", mAssignedSubId=" + mAssignedSubId);
        return subId;
    }

    public static String getImsi(TelephonyManager tm, int subId) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        String imsi = tm.getSubscriberId(subId);
        String cdmaGsmImsi = HwWifiServiceFactory.getHwTelphonyUtils().getCdmaGsmImsi();
        if (cdmaGsmImsi == null || !HwWifiServiceFactory.getHwTelphonyUtils().isCDMASimCard(subId)) {
            return imsi;
        }
        String[] cdmaGsmImsiArray = cdmaGsmImsi.split(",");
        if (2 != cdmaGsmImsiArray.length) {
            return imsi;
        }
        String imsi2 = cdmaGsmImsiArray[1];
        Log.d(TAG, "cdma prefer USIM/GSM imsi");
        return imsi2;
    }
}
