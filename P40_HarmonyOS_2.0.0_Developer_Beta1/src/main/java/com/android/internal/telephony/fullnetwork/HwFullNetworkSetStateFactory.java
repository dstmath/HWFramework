package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkSetStateFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkSetStateBase mFullNetworkSetStateInstance = null;

    public static HwFullNetworkSetStateBase getHwFullNetworkSetState(Context c, CommandsInterfaceEx[] ci, Handler h) {
        HwFullNetworkSetStateBase hwFullNetworkSetStateBase;
        synchronized (LOCK) {
            if (mFullNetworkSetStateInstance == null) {
                setFullNetworkSetStateInstance(c, ci, h);
            }
            hwFullNetworkSetStateBase = mFullNetworkSetStateInstance;
        }
        return hwFullNetworkSetStateBase;
    }

    private static void setFullNetworkSetStateInstance(Context c, CommandsInterfaceEx[] ci, Handler h) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT) {
                mFullNetworkSetStateInstance = new HwFullNetworkSetStateHisi2_0(c, ci, h);
            }
        } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                mFullNetworkSetStateInstance = new HwFullNetworkSetStateMtk(c, ci, h);
            }
        } else if (HwFullNetworkConfigInner.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            mFullNetworkSetStateInstance = new HwFullNetworkSetStateQcom2_0(c, ci, h);
        }
    }
}
