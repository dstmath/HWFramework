package com.huawei.device.connectivitychrlog;

public class CSubAuth_Chr_Event extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public ENCWifiConnectAuthFailedReason enWifiConnectAuthFailedReason = new ENCWifiConnectAuthFailedReason();
    public LogInt iAP_RSSI = new LogInt();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucIsOnScreen = new LogByte();
    public LogShort usAP_channel = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public CSubAuth_Chr_Event() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("enWifiConnectAuthFailedReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiConnectAuthFailedReason", this.enWifiConnectAuthFailedReason);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("usAP_channel", Integer.valueOf(2));
        this.fieldMap.put("usAP_channel", this.usAP_channel);
        this.lengthMap.put("iAP_RSSI", Integer.valueOf(4));
        this.fieldMap.put("iAP_RSSI", this.iAP_RSSI);
        this.lengthMap.put("ucIsOnScreen", Integer.valueOf(1));
        this.fieldMap.put("ucIsOnScreen", this.ucIsOnScreen);
        this.enSubEventId.setValue("Auth_Chr_Event");
    }
}
