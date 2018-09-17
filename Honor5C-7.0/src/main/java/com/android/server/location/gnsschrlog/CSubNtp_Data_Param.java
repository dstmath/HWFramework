package com.android.server.location.gnsschrlog;

public class CSubNtp_Data_Param extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogLong lNtp_Time;
    public LogLong lReal_Time;
    public LogString strNtp_IpAddr;

    public CSubNtp_Data_Param() {
        this.enSubEventId = new ENCSubEventId();
        this.lReal_Time = new LogLong();
        this.lNtp_Time = new LogLong();
        this.strNtp_IpAddr = new LogString(100);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lReal_Time", Integer.valueOf(8));
        this.fieldMap.put("lReal_Time", this.lReal_Time);
        this.lengthMap.put("lNtp_Time", Integer.valueOf(8));
        this.fieldMap.put("lNtp_Time", this.lNtp_Time);
        this.lengthMap.put("strNtp_IpAddr", Integer.valueOf(100));
        this.fieldMap.put("strNtp_IpAddr", this.strNtp_IpAddr);
        this.enSubEventId.setValue("Ntp_Data_Param");
    }
}
