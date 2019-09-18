package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;

public class HwFullNetworkInitStateFactory {
    private static HwFullNetworkInitStateBase mInitStateInstance;
    private static final Object mLock = new Object();

    public static HwFullNetworkInitStateBase getHwFullNetworkInitState(Context c, CommandsInterface[] ci, Handler h) {
        HwFullNetworkInitStateBase hwFullNetworkInitStateBase;
        synchronized (mLock) {
            if (mInitStateInstance == null) {
                if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                    if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                        mInitStateInstance = new HwFullNetworkInitStateHisi2_0(c, ci, h);
                    } else if (HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        mInitStateInstance = new HwFullNetworkInitStateHisi1_0(c, ci, h);
                    }
                } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        mInitStateInstance = new HwFullNetworkInitStateMtk(c, ci, h);
                    }
                } else if (HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
                    mInitStateInstance = new HwFullNetworkInitStateQcom2_0(c, ci, h);
                }
            }
            hwFullNetworkInitStateBase = mInitStateInstance;
        }
        return hwFullNetworkInitStateBase;
    }
}
