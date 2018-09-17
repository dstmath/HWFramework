package com.huawei.android.pushagent.utils;

public abstract class e {
    public static String va(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj != null) {
            return String.valueOf(obj);
        }
        return null;
    }

    public static String uz(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("\"", "\\\"");
    }
}
