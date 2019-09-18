package com.huawei.device.connectivitychrlog;

public class CSubCPUInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt ipercent = new LogInt();
    public LogLong lmaxFreq = new LogLong();

    public CSubCPUInfo() {
        this.lengthMap.put("enSubEventId", 2);
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ipercent", 4);
        this.fieldMap.put("ipercent", this.ipercent);
        this.lengthMap.put("lmaxFreq", 8);
        this.fieldMap.put("lmaxFreq", this.lmaxFreq);
        this.enSubEventId.setValue("CPUInfo");
    }
}
