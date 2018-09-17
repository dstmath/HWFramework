package com.huawei.device.connectivitychrlog;

public class CSubCPUInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogInt ipercent;
    public LogLong lmaxFreq;

    public CSubCPUInfo() {
        this.enSubEventId = new ENCSubEventId();
        this.ipercent = new LogInt();
        this.lmaxFreq = new LogLong();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ipercent", Integer.valueOf(4));
        this.fieldMap.put("ipercent", this.ipercent);
        this.lengthMap.put("lmaxFreq", Integer.valueOf(8));
        this.fieldMap.put("lmaxFreq", this.lmaxFreq);
        this.enSubEventId.setValue("CPUInfo");
    }
}
