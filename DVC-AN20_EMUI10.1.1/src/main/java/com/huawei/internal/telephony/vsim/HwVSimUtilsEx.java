package com.huawei.internal.telephony.vsim;

import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwVSimUtilsEx {
    public static boolean isVSimCauseCardReload() {
        return HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimCauseCardReload();
    }
}
