package com.huawei.device.connectivitychrlog;

public class CSubDNS extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogInt iFailedCnt;
    public LogInt iresptime;

    public CSubDNS() {
        this.enSubEventId = new ENCSubEventId();
        this.iFailedCnt = new LogInt();
        this.iresptime = new LogInt();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iFailedCnt", Integer.valueOf(4));
        this.fieldMap.put("iFailedCnt", this.iFailedCnt);
        this.lengthMap.put("iresptime", Integer.valueOf(4));
        this.fieldMap.put("iresptime", this.iresptime);
        this.enSubEventId.setValue("DNS");
    }
}
