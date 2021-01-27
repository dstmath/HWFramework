package com.huawei.chr;

import vendor.huawei.hardware.radio_radar.V1_0.RadioIndicationType;

public class RadioIndicationTypeEx {
    public static final int UNSOLICITED = 0;
    public static final int UNSOLICITED_ACK_EXP = 1;

    public static final String toString(int type) {
        return RadioIndicationType.toString(type);
    }
}
