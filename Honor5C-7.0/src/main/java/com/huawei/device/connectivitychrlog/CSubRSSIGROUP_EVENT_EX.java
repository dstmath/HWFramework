package com.huawei.device.connectivitychrlog;

public class CSubRSSIGROUP_EVENT_EX extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogInt iADJACENT_FREQ_APS;
    public LogInt iLAN_RTT_Duration;
    public LogInt iLAN_RTT_Failues;
    public LogInt iLAN_RTT_MAX;
    public LogInt iLAN_RTT_MIN;
    public LogInt iLAN_RTT_Packets;
    public LogInt iSAME_FREQ_APS;
    public LogInt iTcp_RTT_Duration;
    public LogInt iTcp_RTT_Packets;
    public LogInt iTcp_Resend_Packets;
    public LogByte ucRSSIGrpIndex;

    public CSubRSSIGROUP_EVENT_EX() {
        this.enSubEventId = new ENCSubEventId();
        this.ucRSSIGrpIndex = new LogByte();
        this.iSAME_FREQ_APS = new LogInt();
        this.iADJACENT_FREQ_APS = new LogInt();
        this.iTcp_RTT_Duration = new LogInt();
        this.iTcp_RTT_Packets = new LogInt();
        this.iTcp_Resend_Packets = new LogInt();
        this.iLAN_RTT_Duration = new LogInt();
        this.iLAN_RTT_Packets = new LogInt();
        this.iLAN_RTT_Failues = new LogInt();
        this.iLAN_RTT_MAX = new LogInt();
        this.iLAN_RTT_MIN = new LogInt();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucRSSIGrpIndex", Integer.valueOf(1));
        this.fieldMap.put("ucRSSIGrpIndex", this.ucRSSIGrpIndex);
        this.lengthMap.put("iSAME_FREQ_APS", Integer.valueOf(4));
        this.fieldMap.put("iSAME_FREQ_APS", this.iSAME_FREQ_APS);
        this.lengthMap.put("iADJACENT_FREQ_APS", Integer.valueOf(4));
        this.fieldMap.put("iADJACENT_FREQ_APS", this.iADJACENT_FREQ_APS);
        this.lengthMap.put("iTcp_RTT_Duration", Integer.valueOf(4));
        this.fieldMap.put("iTcp_RTT_Duration", this.iTcp_RTT_Duration);
        this.lengthMap.put("iTcp_RTT_Packets", Integer.valueOf(4));
        this.fieldMap.put("iTcp_RTT_Packets", this.iTcp_RTT_Packets);
        this.lengthMap.put("iTcp_Resend_Packets", Integer.valueOf(4));
        this.fieldMap.put("iTcp_Resend_Packets", this.iTcp_Resend_Packets);
        this.lengthMap.put("iLAN_RTT_Duration", Integer.valueOf(4));
        this.fieldMap.put("iLAN_RTT_Duration", this.iLAN_RTT_Duration);
        this.lengthMap.put("iLAN_RTT_Packets", Integer.valueOf(4));
        this.fieldMap.put("iLAN_RTT_Packets", this.iLAN_RTT_Packets);
        this.lengthMap.put("iLAN_RTT_Failues", Integer.valueOf(4));
        this.fieldMap.put("iLAN_RTT_Failues", this.iLAN_RTT_Failues);
        this.lengthMap.put("iLAN_RTT_MAX", Integer.valueOf(4));
        this.fieldMap.put("iLAN_RTT_MAX", this.iLAN_RTT_MAX);
        this.lengthMap.put("iLAN_RTT_MIN", Integer.valueOf(4));
        this.fieldMap.put("iLAN_RTT_MIN", this.iLAN_RTT_MIN);
        this.enSubEventId.setValue("RSSIGROUP_EVENT_EX");
    }
}
