package com.huawei.dmsdp.devicevirtualization;

import java.util.HashMap;
import java.util.Map;

class ReturnCodeConverter {
    private static final Map<Integer, Integer> returnCodeCovertMap = new HashMap() {
        /* class com.huawei.dmsdp.devicevirtualization.ReturnCodeConverter.AnonymousClass1 */

        {
            put(0, 0);
            put(-1, -1);
            put(-2, -2);
            put(-3, -3);
            put(-4, -4);
            put(-5, -5);
            put(-6, -6);
            put(-7, -7);
            put(-8, -8);
            put(-9, -9);
            put(-11, -10);
            put(-10, -11);
            put(-12, -12);
        }
    };

    ReturnCodeConverter() {
    }

    public static int convertReturnCode(int dmsdpret) {
        if (returnCodeCovertMap.containsKey(Integer.valueOf(dmsdpret))) {
            return returnCodeCovertMap.get(Integer.valueOf(dmsdpret)).intValue();
        }
        return -1;
    }
}
