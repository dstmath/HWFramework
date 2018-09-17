package com.android.server.location.gnsschrlog;

public class CSubBrcmPosReferenceInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogLong lTcxo_Offset;
    public LogString strAgc_BDS;
    public LogString strAgc_GLO;
    public LogString strAgc_GPS;
    public LogByte ucAidingStatus;
    public LogByte ucPosSource;
    public LogByte ucTimeSource;

    public CSubBrcmPosReferenceInfo() {
        this.enSubEventId = new ENCSubEventId();
        this.strAgc_GPS = new LogString(5);
        this.strAgc_GLO = new LogString(5);
        this.strAgc_BDS = new LogString(5);
        this.lTcxo_Offset = new LogLong();
        this.ucPosSource = new LogByte();
        this.ucTimeSource = new LogByte();
        this.ucAidingStatus = new LogByte();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAgc_GPS", Integer.valueOf(5));
        this.fieldMap.put("strAgc_GPS", this.strAgc_GPS);
        this.lengthMap.put("strAgc_GLO", Integer.valueOf(5));
        this.fieldMap.put("strAgc_GLO", this.strAgc_GLO);
        this.lengthMap.put("strAgc_BDS", Integer.valueOf(5));
        this.fieldMap.put("strAgc_BDS", this.strAgc_BDS);
        this.lengthMap.put("lTcxo_Offset", Integer.valueOf(8));
        this.fieldMap.put("lTcxo_Offset", this.lTcxo_Offset);
        this.lengthMap.put("ucPosSource", Integer.valueOf(1));
        this.fieldMap.put("ucPosSource", this.ucPosSource);
        this.lengthMap.put("ucTimeSource", Integer.valueOf(1));
        this.fieldMap.put("ucTimeSource", this.ucTimeSource);
        this.lengthMap.put("ucAidingStatus", Integer.valueOf(1));
        this.fieldMap.put("ucAidingStatus", this.ucAidingStatus);
        this.enSubEventId.setValue("BrcmPosReferenceInfo");
    }
}
