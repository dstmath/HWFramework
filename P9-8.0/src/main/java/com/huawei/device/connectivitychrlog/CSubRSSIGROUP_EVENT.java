package com.huawei.device.connectivitychrlog;

public class CSubRSSIGROUP_EVENT extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iADJACENT_FREQ_APS = new LogInt();
    public LogInt iSAME_FREQ_APS = new LogInt();
    public LogByte ucRSSIGrpIndex = new LogByte();

    public CSubRSSIGROUP_EVENT() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucRSSIGrpIndex", Integer.valueOf(1));
        this.fieldMap.put("ucRSSIGrpIndex", this.ucRSSIGrpIndex);
        this.lengthMap.put("iSAME_FREQ_APS", Integer.valueOf(4));
        this.fieldMap.put("iSAME_FREQ_APS", this.iSAME_FREQ_APS);
        this.lengthMap.put("iADJACENT_FREQ_APS", Integer.valueOf(4));
        this.fieldMap.put("iADJACENT_FREQ_APS", this.iADJACENT_FREQ_APS);
        this.enSubEventId.setValue("RSSIGROUP_EVENT");
    }
}
