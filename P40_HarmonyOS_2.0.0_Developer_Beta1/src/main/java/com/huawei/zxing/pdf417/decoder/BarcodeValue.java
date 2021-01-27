package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.pdf417.PDF417Common;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class BarcodeValue {
    private final Map<Integer, Integer> values = new HashMap();

    BarcodeValue() {
    }

    /* access modifiers changed from: package-private */
    public void setValue(int value) {
        Integer confidence = this.values.get(Integer.valueOf(value));
        if (confidence == null) {
            confidence = 0;
        }
        this.values.put(Integer.valueOf(value), Integer.valueOf(confidence.intValue() + 1));
    }

    /* access modifiers changed from: package-private */
    public int[] getValue() {
        int maxConfidence = -1;
        Collection<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : this.values.entrySet()) {
            if (entry.getValue().intValue() > maxConfidence) {
                maxConfidence = entry.getValue().intValue();
                result.clear();
                result.add(entry.getKey());
            } else if (entry.getValue().intValue() == maxConfidence) {
                result.add(entry.getKey());
            }
        }
        return PDF417Common.toIntArray(result);
    }

    public Integer getConfidence(int value) {
        return this.values.get(Integer.valueOf(value));
    }
}
