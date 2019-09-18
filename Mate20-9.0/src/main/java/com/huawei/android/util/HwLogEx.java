package com.huawei.android.util;

import android.util.HwLog;

public class HwLogEx {
    private HwLogEx() {
    }

    public static int dubaiv(String tag, String msg) {
        return HwLog.dubaiv(tag, msg);
    }

    public static int dubaid(String tag, String msg) {
        return HwLog.dubaid(tag, msg);
    }

    public static int dubaii(String tag, String msg) {
        return HwLog.dubaii(tag, msg);
    }

    public static int dubaiw(String tag, String msg) {
        return HwLog.dubaiw(tag, msg);
    }

    public static int dubaie(String tag, String msg) {
        return HwLog.dubaie(tag, msg);
    }
}
