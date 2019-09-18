package com.android.internal.telephony.vsim;

import android.telephony.Rlog;

public class HwVSimLog {
    private static boolean HWDBG = true;
    private static boolean HWFLOW = true;
    private static final boolean HWLOGW_E = true;

    public static void VSimLogD(String name, String s) {
        if (HWDBG) {
            Rlog.d(name, s);
        }
    }

    public static void VSimLogI(String name, String s) {
        if (HWFLOW) {
            Rlog.i(name, s);
        }
    }

    public static void VSimLogE(String name, String s) {
        Rlog.e(name, s);
    }
}
