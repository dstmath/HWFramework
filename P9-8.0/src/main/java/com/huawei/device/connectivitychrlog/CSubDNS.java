package com.huawei.device.connectivitychrlog;

public class CSubDNS extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iFailedCnt = new LogInt();
    public LogInt iresptime = new LogInt();

    public CSubDNS() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iFailedCnt", Integer.valueOf(4));
        this.fieldMap.put("iFailedCnt", this.iFailedCnt);
        this.lengthMap.put("iresptime", Integer.valueOf(4));
        this.fieldMap.put("iresptime", this.iresptime);
        this.enSubEventId.setValue("DNS");
    }
}
