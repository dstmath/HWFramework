package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkDefaultStateFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkDefaultStateBase mDefaultStateBaseInstance;

    public static HwFullNetworkDefaultStateBase getHwFullNetworkDefaultState(Context c, CommandsInterfaceEx[] ci, Handler h) {
        HwFullNetworkDefaultStateBase hwFullNetworkDefaultStateBase;
        synchronized (LOCK) {
            if (mDefaultStateBaseInstance == null) {
                setDefaultStateBaseInstance(c, ci, h);
            }
            hwFullNetworkDefaultStateBase = mDefaultStateBaseInstance;
        }
        return hwFullNetworkDefaultStateBase;
    }

    private static void setDefaultStateBaseInstance(Context c, CommandsInterfaceEx[] ci, Handler h) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT) {
                mDefaultStateBaseInstance = new HwFullNetworkDefaultStateHisi2_0(c, ci, h);
            }
        } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                mDefaultStateBaseInstance = new HwFullNetworkDefaultStateMtk(c, ci, h);
            }
        } else if (HwFullNetworkConfigInner.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            mDefaultStateBaseInstance = new HwFullNetworkDefaultStateQcom2_0(c, ci, h);
        }
    }
}
