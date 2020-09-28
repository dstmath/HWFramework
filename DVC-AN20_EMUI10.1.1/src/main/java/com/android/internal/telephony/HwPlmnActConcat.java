package com.android.internal.telephony;

import android.telephony.ServiceState;
import android.text.TextUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;

public class HwPlmnActConcat {
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE5 = 5;
    private static final String TAG = "HwPlmnActConcat";
    private static boolean sNeedConcat = HuaweiTelephonyConfigs.isChinaMobile();

    public static boolean needPlmnActConcat() {
        return sNeedConcat;
    }

    public static String getPlmnActConcat(String plmnValue, ServiceState ss) {
        if (TextUtils.isEmpty(plmnValue)) {
            return null;
        }
        int voiceRegState = ServiceStateEx.getVoiceRegState(ss);
        int dataRegState = ServiceStateEx.getDataState(ss);
        int voiceNetworkType = ServiceStateEx.getVoiceNetworkType(ss);
        int dataNetworkType = ServiceStateEx.getDataNetworkType(ss);
        String plmnActValue = plmnValue;
        int networkType = 0;
        if (dataRegState == 0) {
            networkType = dataNetworkType;
            if (ServiceStateEx.getNsaState(ss) >= 2 && ServiceStateEx.getNsaState(ss) <= 5) {
                networkType = ServiceStateEx.getConfigRadioTechnology(ss);
            }
        } else if (voiceRegState == 0) {
            networkType = voiceNetworkType;
        }
        RlogEx.i(TAG, "plmnValue:" + plmnValue + ",voiceNetworkType:" + voiceNetworkType + ",dataNetworkType:" + dataNetworkType + ",NetworkType:" + networkType);
        String act = null;
        int networkClass = TelephonyManagerEx.getNetworkClass(networkType);
        if (networkClass == 1) {
            act = "";
        } else if (networkClass == 2) {
            act = "3G";
        } else if (networkClass == 3) {
            act = "4G";
        } else if (networkClass != 4) {
            RlogEx.e(TAG, "network class unknow");
        } else {
            act = "5G";
        }
        if (act != null && !plmnValue.endsWith(act)) {
            plmnActValue = plmnActValue + act;
        }
        RlogEx.i(TAG, "plmnActValue:" + plmnActValue);
        return plmnActValue;
    }
}
