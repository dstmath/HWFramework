package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucOpenOrClose;
    public LogShort usLen;

    public CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucOpenOrClose = new LogByte();
        this.enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON = new ENCWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucOpenOrClose", Integer.valueOf(1));
        this.fieldMap.put("ucOpenOrClose", this.ucOpenOrClose);
        this.lengthMap.put("enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON", Integer.valueOf(1));
        this.fieldMap.put("enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON", this.enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON);
        this.enEventId.setValue("WIFI_REPEATER_OPEN_OR_CLOSE_FAILED");
        this.usLen.setValue(getTotalLen());
    }
}
