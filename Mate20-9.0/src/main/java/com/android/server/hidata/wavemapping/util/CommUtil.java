package com.android.server.hidata.wavemapping.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommUtil {
    public static int getModalNums(int[] arr) {
        int n = arr.length;
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return arr[0];
        }
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Integer v = freqMap.get(Integer.valueOf(arr[i]));
            freqMap.put(Integer.valueOf(arr[i]), Integer.valueOf(v == null ? 1 : v.intValue() + 1));
        }
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(freqMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> e1, Map.Entry<Integer, Integer> e2) {
                return e2.getValue().intValue() - e1.getValue().intValue();
            }
        });
        return ((Integer) entries.get(0).getKey()).intValue();
    }
}
