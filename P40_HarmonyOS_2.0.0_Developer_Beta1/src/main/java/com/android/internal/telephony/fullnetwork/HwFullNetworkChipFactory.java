package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkChipFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkChipHisi mChipHisi;
    private static HwFullNetworkChipOther mChipOther;

    static void make(Context c, CommandsInterfaceEx[] ci) {
        synchronized (LOCK) {
            if (mChipCommon == null) {
                mChipCommon = HwFullNetworkChipCommon.make(c, ci);
            }
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                if (mChipHisi == null) {
                    mChipHisi = HwFullNetworkChipHisi.make(c, ci);
                }
            } else if (mChipOther == null) {
                mChipOther = HwFullNetworkChipOther.make(c, ci);
            }
        }
    }

    static HwFullNetworkChipCommon getChipCommon() {
        HwFullNetworkChipCommon hwFullNetworkChipCommon;
        synchronized (LOCK) {
            if (mChipCommon != null) {
                hwFullNetworkChipCommon = mChipCommon;
            } else {
                throw new RuntimeException("HwFullNetworkChipCommon Instance can't be called before make()");
            }
        }
        return hwFullNetworkChipCommon;
    }
}
