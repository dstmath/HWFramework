package com.huawei.coauthservice.identitymgr.utils;

public final class ArgsValidationUtils {
    private static final String TAG = ArgsValidationUtils.class.getSimpleName();

    private ArgsValidationUtils() {
    }

    public static boolean isNull(Object... objs) {
        if (objs == null) {
            return true;
        }
        for (Object obj : objs) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(String... args) {
        if (args == null) {
            return true;
        }
        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
