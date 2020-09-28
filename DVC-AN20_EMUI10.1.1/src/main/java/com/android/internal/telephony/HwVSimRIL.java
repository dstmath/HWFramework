package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public final class HwVSimRIL extends CommandsInterfaceEx {
    private static final String RILJ_LOG_TAG = "RILJ-HwVSimRIL";

    HwVSimRIL(Context context, int preferredNetworkType, int cdmaSubscription, int instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    public void setRadioPower(boolean on, Message result) {
        RlogEx.d(RILJ_LOG_TAG, "setRadioPower -> " + on);
        HwVSimRIL.super.setRadioPower(on, result);
        if (on) {
            setApDsFlowCfg(null);
            setDsFlowNvCfg(null);
        }
    }
}
