package com.huawei.g11n.tmr.address;

import java.util.List;

public class AddressTmr {
    public static int[] getAddr(String str) {
        List search = new SerEn().search(str);
        if (search != null) {
            int[] iArr = new int[((search.size() * 2) + 1)];
            iArr[0] = search.size();
            for (int i = 0; i < search.size(); i++) {
                iArr[(i * 2) + 1] = ((Match) search.get(i)).getStartPos().intValue();
                int intValue = ((Match) search.get(i)).getEndPos().intValue();
                if (intValue > 1) {
                    intValue--;
                }
                iArr[(i * 2) + 2] = intValue;
            }
            return iArr;
        }
        return new int[]{0};
    }
}
