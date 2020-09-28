package com.huawei.g11n.tmr.address;

import java.util.List;

public class AddressTmr {
    public static int[] getAddr(String txtMessage) {
        List<Match> result = new SerEn().search(txtMessage);
        if (result == null) {
            return new int[]{0};
        }
        int[] r = new int[((result.size() * 2) + 1)];
        r[0] = result.size();
        for (int index = 0; index < result.size(); index++) {
            r[(index * 2) + 1] = result.get(index).getStartPos().intValue();
            int end = result.get(index).getEndPos().intValue();
            if (end > 1) {
                end--;
            }
            r[(index * 2) + 2] = end;
        }
        return r;
    }
}
