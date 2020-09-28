package com.huawei.dmsdp.devicevirtualization;

import java.util.HashMap;
import java.util.Map;

public class EventTypeConverter {
    private static final Map<Integer, Integer> deviceTypeCovertMap = new HashMap() {
        /* class com.huawei.dmsdp.devicevirtualization.EventTypeConverter.AnonymousClass1 */

        {
            put(101, 101);
            put(102, 102);
            put(103, 103);
            put(107, 107);
            put(108, 108);
            put(110, 110);
            put(204, 204);
            put(205, 205);
            put(206, 206);
            put(-5, 207);
            put(-1, 206);
            put(-2, 206);
        }
    };

    public static int convertEventType(int dmsdpret) {
        if (deviceTypeCovertMap.containsKey(Integer.valueOf(dmsdpret))) {
            return deviceTypeCovertMap.get(Integer.valueOf(dmsdpret)).intValue();
        }
        return 206;
    }
}
