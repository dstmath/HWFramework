package com.huawei.security.dpermission.utils;

import java.util.Objects;

public final class LogUtil {
    private static final int MASK_LENGTH = 4;
    private static final String MASK_STRING = "******";

    private LogUtil() {
    }

    public static String mask(String str) {
        if (Objects.isNull(str) || str.length() < 4) {
            return str;
        }
        return MASK_STRING + str.substring(str.length() - 4);
    }
}
