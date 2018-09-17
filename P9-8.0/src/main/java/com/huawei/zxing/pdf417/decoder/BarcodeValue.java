package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.pdf417.PDF417Common;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final class BarcodeValue {
    private final Map<Integer, Integer> values = new HashMap();

    BarcodeValue() {
    }

    void setValue(int value) {
        Integer confidence = (Integer) this.values.get(Integer.valueOf(value));
        if (confidence == null) {
            confidence = Integer.valueOf(0);
        }
        this.values.put(Integer.valueOf(value), Integer.valueOf(confidence.intValue() + 1));
    }

    int[] getValue() {
        int maxConfidence = -1;
        Collection<Integer> result = new ArrayList();
        for (Entry<Integer, Integer> entry : this.values.entrySet()) {
            if (((Integer) entry.getValue()).intValue() > maxConfidence) {
                maxConfidence = ((Integer) entry.getValue()).intValue();
                result.clear();
                result.add((Integer) entry.getKey());
            } else if (((Integer) entry.getValue()).intValue() == maxConfidence) {
                result.add((Integer) entry.getKey());
            }
        }
        return PDF417Common.toIntArray(result);
    }

    public Integer getConfidence(int value) {
        return (Integer) this.values.get(Integer.valueOf(value));
    }
}
