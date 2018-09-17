package com.huawei.device.connectivitychrlog;

public class CSubMemInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iMemLoad = new LogInt();

    public CSubMemInfo() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iMemLoad", Integer.valueOf(4));
        this.fieldMap.put("iMemLoad", this.iMemLoad);
        this.enSubEventId.setValue("MemInfo");
    }
}
