package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkInitStateFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkInitStateBase mInitStateInstance;

    public static HwFullNetworkInitStateBase getHwFullNetworkInitState(Context c, CommandsInterfaceEx[] ci, Handler h) {
        HwFullNetworkInitStateBase hwFullNetworkInitStateBase;
        synchronized (LOCK) {
            if (mInitStateInstance == null) {
                setInitStateInstance(c, ci, h);
            }
            hwFullNetworkInitStateBase = mInitStateInstance;
        }
        return hwFullNetworkInitStateBase;
    }

    private static void setInitStateInstance(Context c, CommandsInterfaceEx[] ci, Handler h) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT) {
                mInitStateInstance = new HwFullNetworkInitStateHisi2_0(c, ci, h);
            }
        } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                mInitStateInstance = new HwFullNetworkInitStateMtk(c, ci, h);
            }
        } else if (HwFullNetworkConfigInner.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            mInitStateInstance = new HwFullNetworkInitStateQcom2_0(c, ci, h);
        }
    }
}
