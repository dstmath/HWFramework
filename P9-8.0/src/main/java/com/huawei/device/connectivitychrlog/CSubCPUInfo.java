package com.huawei.device.connectivitychrlog;

public class CSubCPUInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt ipercent = new LogInt();
    public LogLong lmaxFreq = new LogLong();

    public CSubCPUInfo() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ipercent", Integer.valueOf(4));
        this.fieldMap.put("ipercent", this.ipercent);
        this.lengthMap.put("lmaxFreq", Integer.valueOf(8));
        this.fieldMap.put("lmaxFreq", this.lmaxFreq);
        this.enSubEventId.setValue("CPUInfo");
    }
}
