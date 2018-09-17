package com.huawei.device.connectivitychrlog;

public class ENCSubEventId extends Cenum {
    public ENCSubEventId() {
        this.map.put("BTStatus", Integer.valueOf(11));
        this.map.put("NET_CFG", Integer.valueOf(12));
        this.map.put("CellID", Integer.valueOf(13));
        this.map.put("CPUInfo", Integer.valueOf(14));
        this.map.put("MemInfo", Integer.valueOf(15));
        this.map.put("TRAFFIC_GROUND", Integer.valueOf(16));
        this.map.put("TCP_STATIST", Integer.valueOf(17));
        this.map.put("RSSIGROUP_EVENT", Integer.valueOf(18));
        this.map.put("RSSIGROUP_EVENT_EX", Integer.valueOf(19));
        this.map.put("DNS", Integer.valueOf(20));
        this.map.put("PacketCount", Integer.valueOf(21));
        this.map.put("WL_COUNTERS", Integer.valueOf(22));
        this.map.put("ApRoaming", Integer.valueOf(23));
        this.map.put("Assoc_Chr_Event", Integer.valueOf(24));
        this.map.put("Auth_Chr_Event", Integer.valueOf(25));
        this.map.put("DHCP_Chr_Event", Integer.valueOf(26));
        setLength(2);
    }
}
