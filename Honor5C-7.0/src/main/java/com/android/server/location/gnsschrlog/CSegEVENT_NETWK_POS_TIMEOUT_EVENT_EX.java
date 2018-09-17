package com.android.server.location.gnsschrlog;

public class CSegEVENT_NETWK_POS_TIMEOUT_EVENT_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info;
    public ENCEventId enEventId;
    public LogInt ia_ucPosTime;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogByte ucLocSetStatus;
    public LogByte ucNetworkStatus;
    public LogByte ucPosMethod;
    public LogShort usLen;

    public CSegEVENT_NETWK_POS_TIMEOUT_EVENT_EX() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.ucErrorCode = new LogByte();
        this.tmTimeStamp = new LogDate(6);
        this.ia_ucPosTime = new LogInt();
        this.ucPosMethod = new LogByte();
        this.ucLocSetStatus = new LogByte();
        this.ucCardIndex = new LogByte();
        this.ucNetworkStatus = new LogByte();
        this.aucExt_info = new LogByteArray(8192);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ia_ucPosTime", Integer.valueOf(4));
        this.fieldMap.put("ia_ucPosTime", this.ia_ucPosTime);
        this.lengthMap.put("ucPosMethod", Integer.valueOf(1));
        this.fieldMap.put("ucPosMethod", this.ucPosMethod);
        this.lengthMap.put("ucLocSetStatus", Integer.valueOf(1));
        this.fieldMap.put("ucLocSetStatus", this.ucLocSetStatus);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucNetworkStatus", Integer.valueOf(1));
        this.fieldMap.put("ucNetworkStatus", this.ucNetworkStatus);
        this.lengthMap.put("aucExt_info", Integer.valueOf(8192));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("NETWK_POS_TIMEOUT_EVENT_EX");
        this.usLen.setValue(getTotalLen());
    }
}
