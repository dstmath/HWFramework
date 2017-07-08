package com.android.server.location.gnsschrlog;

public class CSubVdrDisableTime extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogLong lVdr_DisableTime;

    public CSubVdrDisableTime() {
        this.enSubEventId = new ENCSubEventId();
        this.lVdr_DisableTime = new LogLong();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lVdr_DisableTime", Integer.valueOf(8));
        this.fieldMap.put("lVdr_DisableTime", this.lVdr_DisableTime);
        this.enSubEventId.setValue("VdrDisableTime");
    }
}
