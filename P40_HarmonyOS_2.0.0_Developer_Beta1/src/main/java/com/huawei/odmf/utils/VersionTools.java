package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;

public class VersionTools {
    private VersionTools() {
    }

    public static String parseToStringVersion(int... iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new ODMFIllegalArgumentException("Execute parseToStringVersion failed : The versions is null.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iArr.length; i++) {
            sb.append(iArr[i]);
            if (i < iArr.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public static int compareVersion(String str, String str2) {
        if (str == null || str2 == null) {
            throw new ODMFIllegalArgumentException("Execute compareVersion failed : Some of the version is null.");
        } else if (!JudgeUtils.checkVersion(str) || !JudgeUtils.checkVersion(str2)) {
            throw new ODMFIllegalArgumentException("Execute compareVersion failed : The string version pattern is wrong.");
        } else {
            String[] split = str.split("\\.");
            String[] split2 = str2.split("\\.");
            if (split.length == split2.length) {
                int length = split.length;
                for (int i = 0; i < length; i++) {
                    if (Integer.parseInt(split[i]) > Integer.parseInt(split2[i])) {
                        return 1;
                    }
                    if (Integer.parseInt(split[i]) < Integer.parseInt(split2[i])) {
                        return -1;
                    }
                }
                return 0;
            }
            throw new ODMFIllegalArgumentException("Execute compareVersion failed : The version1's patterns is different from version2's.");
        }
    }
}
