package com.huawei.device.connectivitychrlog;

public class CSubCellID extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogInt iCID;
    public LogInt iLAC;
    public LogString strMCC;
    public LogString strMNC;

    public CSubCellID() {
        this.enSubEventId = new ENCSubEventId();
        this.iCID = new LogInt();
        this.iLAC = new LogInt();
        this.strMCC = new LogString(8);
        this.strMNC = new LogString(8);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iCID", Integer.valueOf(4));
        this.fieldMap.put("iCID", this.iCID);
        this.lengthMap.put("iLAC", Integer.valueOf(4));
        this.fieldMap.put("iLAC", this.iLAC);
        this.lengthMap.put("strMCC", Integer.valueOf(8));
        this.fieldMap.put("strMCC", this.strMCC);
        this.lengthMap.put("strMNC", Integer.valueOf(8));
        this.fieldMap.put("strMNC", this.strMNC);
        this.enSubEventId.setValue("CellID");
    }
}
