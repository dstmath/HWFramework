package com.android.server.location.gnsschrlog;

public class CSubWifiApInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogInt iAP_RSSI;
    public LogString strAP_Bssid;
    public LogString strAP_SSID;

    public CSubWifiApInfo() {
        this.enSubEventId = new ENCSubEventId();
        this.strAP_Bssid = new LogString(17);
        this.strAP_SSID = new LogString(32);
        this.iAP_RSSI = new LogInt();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAP_Bssid", Integer.valueOf(17));
        this.fieldMap.put("strAP_Bssid", this.strAP_Bssid);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("iAP_RSSI", Integer.valueOf(4));
        this.fieldMap.put("iAP_RSSI", this.iAP_RSSI);
        this.enSubEventId.setValue("WifiApInfo");
    }
}
