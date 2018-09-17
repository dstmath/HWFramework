package com.huawei.android.pushagent.utils;

import java.util.regex.Pattern;

public abstract class g {
    public static boolean vg(String str) {
        return vh("^[0-9]*$", str);
    }

    private static boolean vh(String str, String str2) {
        return Pattern.compile(str).matcher(str2).matches();
    }
}
