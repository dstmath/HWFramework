package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkCheckStateFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkCheckStateBase mCheckStateBaseInstance;

    public static HwFullNetworkCheckStateBase getHwFullNetworkCheckState(Context c, CommandsInterfaceEx[] ci, Handler h) {
        HwFullNetworkCheckStateBase hwFullNetworkCheckStateBase;
        synchronized (LOCK) {
            if (mCheckStateBaseInstance == null) {
                setCheckStateBaseInstance(c, ci, h);
            }
            hwFullNetworkCheckStateBase = mCheckStateBaseInstance;
        }
        return hwFullNetworkCheckStateBase;
    }

    private static void setCheckStateBaseInstance(Context c, CommandsInterfaceEx[] ci, Handler h) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT) {
                mCheckStateBaseInstance = new HwFullNetworkCheckStateHisi2_0(c, ci, h);
            }
        } else if (!HuaweiTelephonyConfigs.isMTKPlatform()) {
            mCheckStateBaseInstance = new HwFullNetworkCheckStateQcom2_0(c, ci, h);
        } else if (TelephonyManagerEx.isMultiSimEnabled()) {
            mCheckStateBaseInstance = new HwFullNetworkCheckStateMtk(c, ci, h);
        }
    }
}
