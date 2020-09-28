package com.huawei.device.connectivitychrlog;

public class CSubApRoaming extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iFirst_Channel = new LogInt();
    public LogInt iFirst_Rssi = new LogInt();
    public LogInt iSecond_Channel = new LogInt();
    public LogInt iSecond_Rssi = new LogInt();

    public CSubApRoaming() {
        this.lengthMap.put("enSubEventId", 2);
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iFirst_Rssi", 4);
        this.fieldMap.put("iFirst_Rssi", this.iFirst_Rssi);
        this.lengthMap.put("iFirst_Channel", 4);
        this.fieldMap.put("iFirst_Channel", this.iFirst_Channel);
        this.lengthMap.put("iSecond_Rssi", 4);
        this.fieldMap.put("iSecond_Rssi", this.iSecond_Rssi);
        this.lengthMap.put("iSecond_Channel", 4);
        this.fieldMap.put("iSecond_Channel", this.iSecond_Channel);
        this.enSubEventId.setValue("ApRoaming");
    }
}
