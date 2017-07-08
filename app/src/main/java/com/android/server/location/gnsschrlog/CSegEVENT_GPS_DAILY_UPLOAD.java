package com.android.server.location.gnsschrlog;

public class CSegEVENT_GPS_DAILY_UPLOAD extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogInt iflowerroruploadCnt;
    public LogInt igpserroruploadCnt;
    public LogInt inetworktimeoutCnt;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;
    public LogShort usSubErrorCode;

    public CSegEVENT_GPS_DAILY_UPLOAD() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.usSubErrorCode = new LogShort();
        this.inetworktimeoutCnt = new LogInt();
        this.iflowerroruploadCnt = new LogInt();
        this.igpserroruploadCnt = new LogInt();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("inetworktimeoutCnt", Integer.valueOf(4));
        this.fieldMap.put("inetworktimeoutCnt", this.inetworktimeoutCnt);
        this.lengthMap.put("iflowerroruploadCnt", Integer.valueOf(4));
        this.fieldMap.put("iflowerroruploadCnt", this.iflowerroruploadCnt);
        this.lengthMap.put("igpserroruploadCnt", Integer.valueOf(4));
        this.fieldMap.put("igpserroruploadCnt", this.igpserroruploadCnt);
        this.enEventId.setValue("GPS_DAILY_UPLOAD");
        this.usLen.setValue(getTotalLen());
    }
}
