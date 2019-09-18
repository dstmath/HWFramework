package com.huawei.odmf.utils;

import java.util.Arrays;
import java.util.Collection;

public class StringUtil {
    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public static String join(Collection<?> list, String separator) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (Object item : list) {
            if (item != null) {
                b.append(separator).append(item);
            }
        }
        if (b.length() == 0) {
            return "";
        }
        return b.toString().substring(separator.length());
    }

    public static <T> String join(T[] array, String separator) {
        return join((Collection<?>) Arrays.asList(array), separator);
    }
}
