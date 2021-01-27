package ohos.msdp.devicevirtualization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ReturnCodeConverter {
    private static final Map<Integer, Integer> RETURN_CODE_COVERT_MAP;

    ReturnCodeConverter() {
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(0, 0);
        hashMap.put(-1, -1);
        hashMap.put(-2, -2);
        hashMap.put(-3, -3);
        hashMap.put(-4, -4);
        hashMap.put(-5, -5);
        hashMap.put(-6, -6);
        hashMap.put(-7, -7);
        hashMap.put(-8, -8);
        hashMap.put(-9, -9);
        hashMap.put(-11, -10);
        hashMap.put(-10, -11);
        hashMap.put(-12, -12);
        RETURN_CODE_COVERT_MAP = Collections.unmodifiableMap(hashMap);
    }

    public static int convertReturnCode(int i) {
        if (RETURN_CODE_COVERT_MAP.containsKey(Integer.valueOf(i))) {
            return RETURN_CODE_COVERT_MAP.get(Integer.valueOf(i)).intValue();
        }
        return -1;
    }
}
