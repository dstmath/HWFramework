package com.huawei.device.connectivitychrlog;

public class CSubCellID extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iCID = new LogInt();
    public LogInt iEARFCN = new LogInt();
    public LogInt iLAC = new LogInt();
    public LogInt iRSSI = new LogInt();
    public LogString strMCC = new LogString(8);
    public LogString strMNC = new LogString(8);

    public CSubCellID() {
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
        this.lengthMap.put("iEARFCN", Integer.valueOf(4));
        this.fieldMap.put("iEARFCN", this.iEARFCN);
        this.lengthMap.put("iRSSI", Integer.valueOf(4));
        this.fieldMap.put("iRSSI", this.iRSSI);
        this.enSubEventId.setValue("CellID");
    }
}
