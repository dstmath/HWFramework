package com.android.server.security.tsmagent.utils;

public class StringUtil {
    public static boolean isTrimedEmpty(String str) {
        return str != null ? isEmpty(str.trim()) : true;
    }

    public static boolean isEmpty(String str) {
        return str != null ? str.isEmpty() : true;
    }
}
