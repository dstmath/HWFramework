package com.android.internal.telephony.vsim;

import com.huawei.android.telephony.RlogEx;

public class HwVSimLog {
    public static void VSimLogD(String name, String s) {
        RlogEx.i(name, s);
    }

    public static void VSimLogI(String name, String s) {
        RlogEx.i(name, s);
    }

    public static void VSimLogE(String name, String s) {
        RlogEx.e(name, s);
    }
}
