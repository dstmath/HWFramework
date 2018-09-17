package com.android.server.location.gnsschrlog;

public class CSegEVENT_GNSS_DATA_COLLECT_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCNetworkStatus enNetworkStatus;
    public LogInt iCell_Lac;
    public LogInt iCell_Mcc;
    public LogInt iCell_Mnc;
    public LogString strNtpIP;
    public LogString strNtpServer;
    public LogString strWifi_Bssid;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogShort usCell_NID;
    public LogShort usCell_SID;
    public LogShort usLen;

    public CSegEVENT_GNSS_DATA_COLLECT_EVENT() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucErrorCode = new LogByte();
        this.enNetworkStatus = new ENCNetworkStatus();
        this.iCell_Mcc = new LogInt();
        this.iCell_Mnc = new LogInt();
        this.iCell_Lac = new LogInt();
        this.usCell_SID = new LogShort();
        this.usCell_NID = new LogShort();
        this.strWifi_Bssid = new LogString(17);
        this.strNtpServer = new LogString(50);
        this.strNtpIP = new LogString(50);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("enNetworkStatus", Integer.valueOf(1));
        this.fieldMap.put("enNetworkStatus", this.enNetworkStatus);
        this.lengthMap.put("iCell_Mcc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mcc", this.iCell_Mcc);
        this.lengthMap.put("iCell_Mnc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mnc", this.iCell_Mnc);
        this.lengthMap.put("iCell_Lac", Integer.valueOf(4));
        this.fieldMap.put("iCell_Lac", this.iCell_Lac);
        this.lengthMap.put("usCell_SID", Integer.valueOf(2));
        this.fieldMap.put("usCell_SID", this.usCell_SID);
        this.lengthMap.put("usCell_NID", Integer.valueOf(2));
        this.fieldMap.put("usCell_NID", this.usCell_NID);
        this.lengthMap.put("strWifi_Bssid", Integer.valueOf(17));
        this.fieldMap.put("strWifi_Bssid", this.strWifi_Bssid);
        this.lengthMap.put("strNtpServer", Integer.valueOf(50));
        this.fieldMap.put("strNtpServer", this.strNtpServer);
        this.lengthMap.put("strNtpIP", Integer.valueOf(50));
        this.fieldMap.put("strNtpIP", this.strNtpIP);
        this.enEventId.setValue("GNSS_DATA_COLLECT_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
