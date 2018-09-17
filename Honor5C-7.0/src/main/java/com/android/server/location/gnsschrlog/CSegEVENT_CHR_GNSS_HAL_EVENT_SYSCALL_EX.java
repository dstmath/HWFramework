package com.android.server.location.gnsschrlog;

public class CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info;
    public ENCEventId enEventId;
    public ENCGpsSysCallErrorReason enGpsSysCallErrorReason;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL_EX() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enGpsSysCallErrorReason = new ENCGpsSysCallErrorReason();
        this.aucExt_info = new LogByteArray(8192);
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
        this.lengthMap.put("aucExt_info", Integer.valueOf(8192));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("CHR_GNSS_HAL_EVENT_SYSCALL_EX");
        this.usLen.setValue(getTotalLen());
    }
}
