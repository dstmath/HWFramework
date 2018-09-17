package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WIFIPRO_FREE_AP_INFO extends ChrLogBaseEventModel {
    public LogByteArray aucAP1Ssid;
    public LogByteArray aucAP2Ssid;
    public LogByteArray aucAP3Ssid;
    public ENCEventId enEventId;
    public LogString strAP1BSsid;
    public LogString strAP2BSsid;
    public LogString strAP3BSsid;
    public LogString strcommunicatID;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_WIFI_WIFIPRO_FREE_AP_INFO() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.aucAP1Ssid = new LogByteArray(32);
        this.aucAP2Ssid = new LogByteArray(32);
        this.aucAP3Ssid = new LogByteArray(32);
        this.strAP1BSsid = new LogString(17);
        this.strAP2BSsid = new LogString(17);
        this.strAP3BSsid = new LogString(17);
        this.strcommunicatID = new LogString(32);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("aucAP1Ssid ", Integer.valueOf(32));
        this.fieldMap.put("aucAP1Ssid ", this.aucAP1Ssid);
        this.lengthMap.put("aucAP2Ssid ", Integer.valueOf(32));
        this.fieldMap.put("aucAP2Ssid ", this.aucAP2Ssid);
        this.lengthMap.put("aucAP3Ssid ", Integer.valueOf(32));
        this.fieldMap.put("aucAP3Ssid ", this.aucAP3Ssid);
        this.lengthMap.put("strAP1BSsid", Integer.valueOf(17));
        this.fieldMap.put("strAP1BSsid", this.strAP1BSsid);
        this.lengthMap.put("strAP2BSsid", Integer.valueOf(17));
        this.fieldMap.put("strAP2BSsid", this.strAP2BSsid);
        this.lengthMap.put("strAP3BSsid", Integer.valueOf(17));
        this.fieldMap.put("strAP3BSsid", this.strAP3BSsid);
        this.lengthMap.put("strcommunicatID", Integer.valueOf(32));
        this.fieldMap.put("strcommunicatID", this.strcommunicatID);
        this.enEventId.setValue("WIFI_WIFIPRO_FREE_AP_INFO");
        this.usLen.setValue(getTotalLen());
    }
}
