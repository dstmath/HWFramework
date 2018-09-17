package com.android.server.location.gnsschrlog;

public class CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public ENCGpsSysCallErrorReason enGpsSysCallErrorReason;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enGpsSysCallErrorReason = new ENCGpsSysCallErrorReason();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enGpsSysCallErrorReason", Integer.valueOf(1));
        this.fieldMap.put("enGpsSysCallErrorReason", this.enGpsSysCallErrorReason);
        this.enEventId.setValue("CHR_GNSS_HAL_EVENT_SYSCALL");
        this.usLen.setValue(getTotalLen());
    }
}
