package ohos.msdp.devicevirtualization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class EventTypeConverter {
    private static final Map<Integer, Integer> EVENT_TYPE_COVERT_MAP;

    EventTypeConverter() {
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(101, 101);
        hashMap.put(102, 102);
        hashMap.put(103, 103);
        Integer valueOf = Integer.valueOf((int) EventType.EVENT_DEVICE_SHOW_PIN_INPUT);
        hashMap.put(valueOf, valueOf);
        hashMap.put(108, 108);
        Integer valueOf2 = Integer.valueOf((int) EventType.EVENT_DEVICE_ACTIVE_DISCONNECT);
        hashMap.put(valueOf2, valueOf2);
        hashMap.put(Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_ENABLE), Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_ENABLE));
        hashMap.put(Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_DISABLE), Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_DISABLE));
        Integer valueOf3 = Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        hashMap.put(valueOf3, valueOf3);
        hashMap.put(-5, Integer.valueOf((int) EventType.EVENT_DEVICE_CAPABILITY_BUSY));
        hashMap.put(-1, valueOf3);
        hashMap.put(-2, valueOf3);
        EVENT_TYPE_COVERT_MAP = Collections.unmodifiableMap(hashMap);
    }

    public static int convertEventType(int i) {
        return EVENT_TYPE_COVERT_MAP.containsKey(Integer.valueOf(i)) ? EVENT_TYPE_COVERT_MAP.get(Integer.valueOf(i)).intValue() : EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL;
    }
}
