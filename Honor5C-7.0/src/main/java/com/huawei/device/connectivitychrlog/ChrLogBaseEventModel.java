package com.huawei.device.connectivitychrlog;

import java.util.Map.Entry;

public class ChrLogBaseEventModel extends ChrLogBaseModel {
    protected int getTotalLen() {
        int totalBytes = 0;
        int firsttwo = 0;
        for (Entry entry : this.lengthMap.entrySet()) {
            int firsttwo2 = firsttwo + 1;
            if (firsttwo > 1) {
                totalBytes += ((Integer) entry.getValue()).intValue();
            }
            firsttwo = firsttwo2;
        }
        return totalBytes;
    }
}
