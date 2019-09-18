package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;

public class VersionTools {
    public static String parseToStringVersion(int... versions) {
        if (versions == null || versions.length == 0) {
            throw new ODMFIllegalArgumentException("Execute parseToStringVersion failed : The versions is null.");
        }
        StringBuilder stringVersion = new StringBuilder();
        for (int i = 0; i < versions.length; i++) {
            stringVersion.append(versions[i]);
            if (i < versions.length - 1) {
                stringVersion.append(".");
            }
        }
        return stringVersion.toString();
    }

    public static int compareVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            throw new ODMFIllegalArgumentException("Execute compareVersion failed : Some of the version is null.");
        } else if (!JudgeUtils.checkVersion(version1) || !JudgeUtils.checkVersion(version2)) {
            throw new ODMFIllegalArgumentException("Execute compareVersion failed : The string version pattern is wrong.");
        } else {
            String[] stringArray1 = version1.split("\\.");
            String[] stringArray2 = version2.split("\\.");
            if (stringArray1.length != stringArray2.length) {
                throw new ODMFIllegalArgumentException("Execute compareVersion failed :  The version1's patterns is different from version2's.");
            }
            int length = stringArray1.length;
            for (int i = 0; i < length; i++) {
                if (Integer.parseInt(stringArray1[i]) > Integer.parseInt(stringArray2[i])) {
                    return 1;
                }
                if (Integer.parseInt(stringArray1[i]) < Integer.parseInt(stringArray2[i])) {
                    return -1;
                }
            }
            return 0;
        }
    }
}
