package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;

public class CSegEVENT_WIFI_SCANED_AP_INFO extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogString strBSSID;
    public LogString strapVendorInfo;
    public LogDate tmTimeStamp;
    public LogByte ucApStreamInfo;
    public LogByte ucCardIndex;
    public LogByte ucTxMcsSet;
    public LogShort usLen;

    public CSegEVENT_WIFI_SCANED_AP_INFO() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucApStreamInfo = new LogByte();
        this.strBSSID = new LogString(18);
        this.ucTxMcsSet = new LogByte();
        this.strapVendorInfo = new LogString(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucApStreamInfo", Integer.valueOf(1));
        this.fieldMap.put("ucApStreamInfo", this.ucApStreamInfo);
        this.lengthMap.put("strBSSID", Integer.valueOf(18));
        this.fieldMap.put("strBSSID", this.strBSSID);
        this.lengthMap.put("ucTxMcsSet", Integer.valueOf(1));
        this.fieldMap.put("ucTxMcsSet", this.ucTxMcsSet);
        this.lengthMap.put("strapVendorInfo", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE));
        this.fieldMap.put("strapVendorInfo", this.strapVendorInfo);
        this.enEventId.setValue("WIFI_SCANED_AP_INFO");
        this.usLen.setValue(getTotalLen());
    }
}
