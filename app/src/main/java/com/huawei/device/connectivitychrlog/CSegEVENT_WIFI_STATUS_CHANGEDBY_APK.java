package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_STATUS_CHANGEDBY_APK extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogString strApkName;
    public LogString strSsid;
    public LogString strUserAction;
    public LogDate tmTimeStamp;
    public LogByte ucApkAction;
    public LogByte ucApkChangeTimes;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_WIFI_STATUS_CHANGEDBY_APK() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucApkAction = new LogByte();
        this.strApkName = new LogString(32);
        this.ucApkChangeTimes = new LogByte();
        this.strSsid = new LogString(32);
        this.strUserAction = new LogString(32);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucApkAction", Integer.valueOf(1));
        this.fieldMap.put("ucApkAction", this.ucApkAction);
        this.lengthMap.put("strApkName", Integer.valueOf(32));
        this.fieldMap.put("strApkName", this.strApkName);
        this.lengthMap.put("ucApkChangeTimes", Integer.valueOf(1));
        this.fieldMap.put("ucApkChangeTimes", this.ucApkChangeTimes);
        this.lengthMap.put("strSsid", Integer.valueOf(32));
        this.fieldMap.put("strSsid", this.strSsid);
        this.lengthMap.put("strUserAction", Integer.valueOf(32));
        this.fieldMap.put("strUserAction", this.strUserAction);
        this.enEventId.setValue("WIFI_STATUS_CHANGEDBY_APK");
        this.usLen.setValue(getTotalLen());
    }
}
