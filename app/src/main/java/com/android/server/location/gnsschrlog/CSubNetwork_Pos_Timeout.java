package com.android.server.location.gnsschrlog;

public class CSubNetwork_Pos_Timeout extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogString strIsDataAvailable;
    public LogByte ucSubErrorCode;

    public CSubNetwork_Pos_Timeout() {
        this.enSubEventId = new ENCSubEventId();
        this.ucSubErrorCode = new LogByte();
        this.strIsDataAvailable = new LogString(6);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucSubErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucSubErrorCode", this.ucSubErrorCode);
        this.lengthMap.put("strIsDataAvailable", Integer.valueOf(6));
        this.fieldMap.put("strIsDataAvailable", this.strIsDataAvailable);
        this.enSubEventId.setValue("Network_Pos_Timeout");
    }
}
