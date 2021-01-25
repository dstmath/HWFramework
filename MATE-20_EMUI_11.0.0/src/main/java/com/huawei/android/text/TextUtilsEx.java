package com.huawei.android.text;

import android.text.TextUtils;

public class TextUtilsEx {
    public static boolean isPrintableAsciiOnly(CharSequence str) {
        return TextUtils.isPrintableAsciiOnly(str);
    }
}
