package com.huawei.device.connectivitychrlog;

public class ENCSubEventId extends Cenum {
    public ENCSubEventId() {
        this.map.put("BTStatus", 11);
        this.map.put("NET_CFG", 12);
        this.map.put("CellID", 13);
        this.map.put("CPUInfo", 14);
        this.map.put("MemInfo", 15);
        this.map.put("TRAFFIC_GROUND", 16);
        this.map.put("TCP_STATIST", 17);
        this.map.put("RSSIGROUP_EVENT", 18);
        this.map.put("RSSIGROUP_EVENT_EX", 19);
        this.map.put("DNS", 20);
        this.map.put("PacketCount", 21);
        this.map.put("WL_COUNTERS", 22);
        this.map.put("ApRoaming", 23);
        this.map.put("Assoc_Chr_Event", 24);
        this.map.put("Auth_Chr_Event", 25);
        this.map.put("DHCP_Chr_Event", 26);
        setLength(2);
    }
}
