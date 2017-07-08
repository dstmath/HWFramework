package com.android.server.location.gnsschrlog;

public class CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCGpsInjectError enGpsInjectError;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enGpsInjectError = new ENCGpsInjectError();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enGpsInjectError", Integer.valueOf(1));
        this.fieldMap.put("enGpsInjectError", this.enGpsInjectError);
        this.enEventId.setValue("CHR_GNSS_HAL_EVENT_INJECT");
        this.usLen.setValue(getTotalLen());
    }
}
