package tmsdk.common.utils;

import android.text.TextUtils;

/* compiled from: Unknown */
public class l {
    public static String dk(String str) {
        return str != null ? str : "";
    }

    public static boolean dl(String str) {
        return (str == null || str.equals("")) ? false : true;
    }

    public static boolean dm(String str) {
        return str == null || str.equals("");
    }

    public static boolean dn(String str) {
        return TextUtils.isEmpty(str) || "null".equalsIgnoreCase(str) || "-2".equals(str) || "-1".equals(str);
    }
}
