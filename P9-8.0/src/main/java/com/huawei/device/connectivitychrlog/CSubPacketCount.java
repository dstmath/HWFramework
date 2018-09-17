package com.huawei.device.connectivitychrlog;

public class CSubPacketCount extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iRX_GOOD = new LogInt();
    public LogInt iTX_BAD = new LogInt();
    public LogInt iTX_GOOD = new LogInt();

    public CSubPacketCount() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iTX_GOOD", Integer.valueOf(4));
        this.fieldMap.put("iTX_GOOD", this.iTX_GOOD);
        this.lengthMap.put("iTX_BAD", Integer.valueOf(4));
        this.fieldMap.put("iTX_BAD", this.iTX_BAD);
        this.lengthMap.put("iRX_GOOD", Integer.valueOf(4));
        this.fieldMap.put("iRX_GOOD", this.iRX_GOOD);
        this.enSubEventId.setValue("PacketCount");
    }
}
