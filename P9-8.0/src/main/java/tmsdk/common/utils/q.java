package tmsdk.common.utils;

import android.text.TextUtils;

public class q {
    public static String cI(String str) {
        return str != null ? str : "";
    }

    public static boolean cJ(String str) {
        return (str == null || str.equals("")) ? false : true;
    }

    public static boolean cK(String str) {
        return str == null || str.equals("");
    }

    public static boolean cL(String str) {
        return TextUtils.isEmpty(str) || "null".equalsIgnoreCase(str) || "-2".equals(str) || "-1".equals(str);
    }
}
