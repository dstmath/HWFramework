package com.huawei.device.connectivitychrlog;

public class ENCDualbandExSubEvent extends Cenum {
    public ENCDualbandExSubEvent() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("DUALBAND_HANDOVER_TOO_SLOW", Integer.valueOf(1));
        this.map.put("DUALBAND_HANDOVER_TO_BAD_5G", Integer.valueOf(2));
        this.map.put("DUALBAND_HANDOVER_USER_REJECT", Integer.valueOf(3));
        this.map.put("DUALBAND_HANDOVER_PINGPONG", Integer.valueOf(4));
        this.map.put("DUALBAND_HANDOVER_SNAPSHOP", Integer.valueOf(5));
        setLength(1);
    }
}
