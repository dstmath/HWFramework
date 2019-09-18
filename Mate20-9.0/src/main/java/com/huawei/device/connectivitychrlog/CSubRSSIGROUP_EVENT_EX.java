package com.huawei.device.connectivitychrlog;

public class CSubRSSIGROUP_EVENT_EX extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iADJACENT_FREQ_APS = new LogInt();
    public LogInt iLAN_RTT_Duration = new LogInt();
    public LogInt iLAN_RTT_Failues = new LogInt();
    public LogInt iLAN_RTT_MAX = new LogInt();
    public LogInt iLAN_RTT_MIN = new LogInt();
    public LogInt iLAN_RTT_Packets = new LogInt();
    public LogInt iSAME_FREQ_APS = new LogInt();
    public LogInt iTcp_RTT_Duration = new LogInt();
    public LogInt iTcp_RTT_Packets = new LogInt();
    public LogInt iTcp_Resend_Packets = new LogInt();
    public LogByte ucRSSIGrpIndex = new LogByte();

    public CSubRSSIGROUP_EVENT_EX() {
        this.lengthMap.put("enSubEventId", 2);
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucRSSIGrpIndex", 1);
        this.fieldMap.put("ucRSSIGrpIndex", this.ucRSSIGrpIndex);
        this.lengthMap.put("iSAME_FREQ_APS", 4);
        this.fieldMap.put("iSAME_FREQ_APS", this.iSAME_FREQ_APS);
        this.lengthMap.put("iADJACENT_FREQ_APS", 4);
        this.fieldMap.put("iADJACENT_FREQ_APS", this.iADJACENT_FREQ_APS);
        this.lengthMap.put("iTcp_RTT_Duration", 4);
        this.fieldMap.put("iTcp_RTT_Duration", this.iTcp_RTT_Duration);
        this.lengthMap.put("iTcp_RTT_Packets", 4);
        this.fieldMap.put("iTcp_RTT_Packets", this.iTcp_RTT_Packets);
        this.lengthMap.put("iTcp_Resend_Packets", 4);
        this.fieldMap.put("iTcp_Resend_Packets", this.iTcp_Resend_Packets);
        this.lengthMap.put("iLAN_RTT_Duration", 4);
        this.fieldMap.put("iLAN_RTT_Duration", this.iLAN_RTT_Duration);
        this.lengthMap.put("iLAN_RTT_Packets", 4);
        this.fieldMap.put("iLAN_RTT_Packets", this.iLAN_RTT_Packets);
        this.lengthMap.put("iLAN_RTT_Failues", 4);
        this.fieldMap.put("iLAN_RTT_Failues", this.iLAN_RTT_Failues);
        this.lengthMap.put("iLAN_RTT_MAX", 4);
        this.fieldMap.put("iLAN_RTT_MAX", this.iLAN_RTT_MAX);
        this.lengthMap.put("iLAN_RTT_MIN", 4);
        this.fieldMap.put("iLAN_RTT_MIN", this.iLAN_RTT_MIN);
        this.enSubEventId.setValue("RSSIGROUP_EVENT_EX");
    }
}
