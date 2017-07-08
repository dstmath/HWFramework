package com.android.server.location.gnsschrlog;

public class CSubVdrEnableTime extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogLong lVdr_EnableTime;

    public CSubVdrEnableTime() {
        this.enSubEventId = new ENCSubEventId();
        this.lVdr_EnableTime = new LogLong();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lVdr_EnableTime", Integer.valueOf(8));
        this.fieldMap.put("lVdr_EnableTime", this.lVdr_EnableTime);
        this.enSubEventId.setValue("VdrEnableTime");
    }
}
