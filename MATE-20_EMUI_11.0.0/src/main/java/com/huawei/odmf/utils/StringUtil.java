package com.huawei.odmf.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class StringUtil {
    private StringUtil() {
    }

    public static boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public static String join(Collection<?> collection, String str) {
        if (collection.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            if (obj != null) {
                sb.append(str);
                sb.append(obj);
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.toString().substring(str.length());
    }

    public static <T> String join(T[] tArr, String str) {
        return join(Arrays.asList(tArr), str);
    }

    public static Set<String> array2Set(String[] strArr) {
        if (strArr == null) {
            return new HashSet();
        }
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = strArr[i].trim();
        }
        return new HashSet(Arrays.asList(strArr));
    }
}
