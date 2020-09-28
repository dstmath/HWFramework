package com.huawei.android.util;

import android.content.Context;

public class HwPasswordUtilsCust {
    public static void loadSimplePasswordTable(Context context) {
        HwPasswordUtils.loadSimplePasswordTable(context);
    }

    public static boolean isSimpleAlphaNumericPassword(String password) {
        return HwPasswordUtils.isSimpleAlphaNumericPassword(password);
    }

    public static boolean isOrdinalCharatersPassword(String password) {
        return HwPasswordUtils.isOrdinalCharatersPassword(password);
    }

    public static boolean isSimplePasswordInDictationary(String password) {
        return HwPasswordUtils.isSimplePasswordInDictationary(password);
    }
}
