package com.android.server.location.gnsschrlog;

public class CSubResumePos_Status extends CSubSv_Status {
    public LogInt iResumePosAccuracy;
    public LogInt iResumePosSpeed;
    public LogLong lResumePosTime;

    public CSubResumePos_Status() {
        this.lResumePosTime = new LogLong();
        this.iResumePosSpeed = new LogInt();
        this.iResumePosAccuracy = new LogInt();
        this.lengthMap.put("lResumePosTime", Integer.valueOf(8));
        this.fieldMap.put("lResumePosTime", this.lResumePosTime);
        this.lengthMap.put("iResumePosSpeed", Integer.valueOf(4));
        this.fieldMap.put("iResumePosSpeed", this.iResumePosSpeed);
        this.lengthMap.put("iResumePosAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iResumePosAccuracy", this.iResumePosAccuracy);
        this.enSubEventId.setValue("ResumePos_Status");
    }
}
