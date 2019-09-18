package com.huawei.device.connectivitychrlog;

public class CSubDNS extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iFailedCnt = new LogInt();
    public LogInt iresptime = new LogInt();

    public CSubDNS() {
        this.lengthMap.put("enSubEventId", 2);
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iFailedCnt", 4);
        this.fieldMap.put("iFailedCnt", this.iFailedCnt);
        this.lengthMap.put("iresptime", 4);
        this.fieldMap.put("iresptime", this.iresptime);
        this.enSubEventId.setValue("DNS");
    }
}
