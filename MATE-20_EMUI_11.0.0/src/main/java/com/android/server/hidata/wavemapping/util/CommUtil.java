package com.android.server.hidata.wavemapping.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommUtil {
    private static final int DEFAULT_CAPACITY = 16;

    private CommUtil() {
    }

    public static int getModalNums(int[] arr) {
        int len = arr.length;
        if (len == 0) {
            return 0;
        }
        if (len == 1) {
            return arr[0];
        }
        Map<Integer, Integer> freqMap = new HashMap<>(16);
        for (int num : arr) {
            Integer value = freqMap.get(Integer.valueOf(num));
            freqMap.put(Integer.valueOf(num), Integer.valueOf(value == null ? 1 : value.intValue() + 1));
        }
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(freqMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, Integer>>() {
            /* class com.android.server.hidata.wavemapping.util.CommUtil.AnonymousClass1 */

            public int compare(Map.Entry<Integer, Integer> e1, Map.Entry<Integer, Integer> e2) {
                return e2.getValue().intValue() - e1.getValue().intValue();
            }
        });
        return entries.get(0).getKey().intValue();
    }
}
