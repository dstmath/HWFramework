package com.android.internal.telephony.vsim;

import com.huawei.android.telephony.RlogEx;

public class HwVSimLog {
    public static void VSimLogD(String name, String s) {
        RlogEx.i(name, s);
    }

    public static void VSimLogI(String name, String s) {
        RlogEx.i(name, s);
    }

    public static void debug(String tag, String log) {
        RlogEx.d(tag, log);
    }

    public static void info(String tag, String log) {
        RlogEx.i(tag, log);
    }

    public static void error(String tag, String log) {
        RlogEx.e(tag, log);
    }

    public static void warning(String tag, String log) {
        RlogEx.w(tag, log);
    }
}
