package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;

public class HwFullNetworkSetStateFactory {
    private static HwFullNetworkSetStateBase mFullNetworkSetStateInstance = null;
    private static final Object mLock = new Object();

    public static HwFullNetworkSetStateBase getHwFullNetworkSetState(Context c, CommandsInterface[] ci, Handler h) {
        HwFullNetworkSetStateBase hwFullNetworkSetStateBase;
        synchronized (mLock) {
            if (mFullNetworkSetStateInstance == null) {
                if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                    if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                        mFullNetworkSetStateInstance = new HwFullNetworkSetStateHisi2_0(c, ci, h);
                    } else if (HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        mFullNetworkSetStateInstance = new HwFullNetworkSetStateHisi1_0(c, ci, h);
                    }
                } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        mFullNetworkSetStateInstance = new HwFullNetworkSetStateMtk(c, ci, h);
                    }
                } else if (HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
                    mFullNetworkSetStateInstance = new HwFullNetworkSetStateQcom2_0(c, ci, h);
                }
            }
            hwFullNetworkSetStateBase = mFullNetworkSetStateInstance;
        }
        return hwFullNetworkSetStateBase;
    }
}
