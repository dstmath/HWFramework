package com.android.server.location.gnsschrlog;

public class CSubLosPos_Status extends CSubSv_Status {
    public LogInt iLosPosAccuracy;
    public LogInt iLosPosSpeed;
    public LogLong lLosPosTime;

    public CSubLosPos_Status() {
        this.lLosPosTime = new LogLong();
        this.iLosPosSpeed = new LogInt();
        this.iLosPosAccuracy = new LogInt();
        this.lengthMap.put("lLosPosTime", Integer.valueOf(8));
        this.fieldMap.put("lLosPosTime", this.lLosPosTime);
        this.lengthMap.put("iLosPosSpeed", Integer.valueOf(4));
        this.fieldMap.put("iLosPosSpeed", this.iLosPosSpeed);
        this.lengthMap.put("iLosPosAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iLosPosAccuracy", this.iLosPosAccuracy);
        this.enSubEventId.setValue("LosPos_Status");
    }
}
