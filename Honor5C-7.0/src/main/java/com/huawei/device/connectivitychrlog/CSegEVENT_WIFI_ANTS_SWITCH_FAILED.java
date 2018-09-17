package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_ANTS_SWITCH_FAILED extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCWifiAntsSwitchFailedReason enWifiAntsSwitchFailedReason;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucWifiAntsSwitchDir;
    public LogShort usLen;

    public CSegEVENT_WIFI_ANTS_SWITCH_FAILED() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enWifiAntsSwitchFailedReason = new ENCWifiAntsSwitchFailedReason();
        this.ucWifiAntsSwitchDir = new LogByte();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiAntsSwitchFailedReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiAntsSwitchFailedReason", this.enWifiAntsSwitchFailedReason);
        this.lengthMap.put("ucWifiAntsSwitchDir", Integer.valueOf(1));
        this.fieldMap.put("ucWifiAntsSwitchDir", this.ucWifiAntsSwitchDir);
        this.enEventId.setValue("WIFI_ANTS_SWITCH_FAILED");
        this.usLen.setValue(getTotalLen());
    }
}
