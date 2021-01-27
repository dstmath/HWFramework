package com.android.internal.telephony;

import android.telephony.ServiceState;
import android.text.TextUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;

public class HwPlmnActConcat {
    private static final boolean IS_NEED_CONCAT = HuaweiTelephonyConfigs.isChinaMobile();
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE5 = 5;
    private static final String TAG = "HwPlmnActConcat";

    public static boolean needPlmnActConcat() {
        return IS_NEED_CONCAT;
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
        } else {
            RlogEx.d(TAG, "getPlmnActConcat, no in service");
        }
        RlogEx.i(TAG, "plmnValue:" + plmnValue + ",voiceNetworkType:" + voiceNetworkType + ",dataNetworkType:" + dataNetworkType + ",NetworkType:" + networkType);
        String act = getActByClass(networkType);
        if (act != null && !plmnValue.endsWith(act)) {
            plmnActValue = plmnActValue + act;
        }
        RlogEx.i(TAG, "plmnActValue:" + plmnActValue);
        return plmnActValue;
    }

    private static String getActByClass(int networkType) {
        int networkClass = TelephonyManagerEx.getNetworkClass(networkType);
        if (networkClass == 1) {
            return BuildConfig.FLAVOR;
        }
        if (networkClass == 2) {
            return "3G";
        }
        if (networkClass == 3) {
            return "4G";
        }
        if (networkClass == 4) {
            return "5G";
        }
        RlogEx.e(TAG, "network class unknow");
        return null;
    }
}
