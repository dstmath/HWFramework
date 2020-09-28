package com.huawei.networkit.grs.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ContainerUtils {
    public static final String FIELD_DELIMITER = "&";
    public static final String KEY_VALUE_DELIMITER = "=";

    public static <K, V> boolean equals(Map<K, V> obj, Map<K, V> other) {
        if (obj == other) {
            return true;
        }
        if (obj == null || other == null || obj.size() != other.size()) {
            return false;
        }
        boolean isNotMatch = false;
        Iterator<Map.Entry<K, V>> it = obj.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<K, V> entry = it.next();
            if (other.get(entry.getKey()) != entry.getValue()) {
                isNotMatch = true;
                break;
            }
        }
        if (!isNotMatch) {
            return true;
        }
        return false;
    }

    public static <K, V> int hashCode(Map<K, V> obj) {
        return toString(obj).hashCode();
    }

    public static <K, V> String toString(Map<K, V> obj) {
        if (obj == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<K, V> entry : obj.entrySet()) {
            int count2 = count + 1;
            if (count > 0) {
                sb.append(FIELD_DELIMITER);
            }
            sb.append(entry.getKey().toString());
            sb.append(KEY_VALUE_DELIMITER);
            sb.append(entry.getValue().toString());
            count = count2;
        }
        return sb.toString();
    }

    public static <K> String toString(Set<K> obj) {
        if (obj == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (K key : obj) {
            int count2 = count + 1;
            if (count > 0) {
                sb.append(FIELD_DELIMITER);
            }
            sb.append(key.toString());
            count = count2;
        }
        return sb.toString();
    }
}
