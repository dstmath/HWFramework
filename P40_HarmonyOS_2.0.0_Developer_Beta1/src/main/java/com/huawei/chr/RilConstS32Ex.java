package com.huawei.chr;

import vendor.huawei.hardware.radio_radar.V1_0.RilConstS32;

public class RilConstS32Ex {
    public static final int RIL_UNSOL_HW_RESET_CHR_IND = 2018;
    public static final int RIL_UNSOL_HW_RIL_CHR_IND = 2017;

    public static final String toString(int type) {
        return RilConstS32.toString(type);
    }
}
