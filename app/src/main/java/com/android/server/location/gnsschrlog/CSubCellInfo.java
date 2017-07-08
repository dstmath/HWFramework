package com.android.server.location.gnsschrlog;

public class CSubCellInfo extends ChrLogBaseEventModel {
    public ENCSubEventId enSubEventId;
    public LogInt iCell_Lac;
    public LogInt iCell_Mcc;
    public LogInt iCell_Mnc;
    public LogInt iChannel_Number;
    public LogInt iPhysical_Identity;
    public LogInt iRAT;
    public LogInt iSignal_Strength;
    public LogShort usCell_ID;

    public CSubCellInfo() {
        this.enSubEventId = new ENCSubEventId();
        this.iCell_Mcc = new LogInt();
        this.iCell_Mnc = new LogInt();
        this.iCell_Lac = new LogInt();
        this.usCell_ID = new LogShort();
        this.iSignal_Strength = new LogInt();
        this.iRAT = new LogInt();
        this.iChannel_Number = new LogInt();
        this.iPhysical_Identity = new LogInt();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iCell_Mcc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mcc", this.iCell_Mcc);
        this.lengthMap.put("iCell_Mnc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mnc", this.iCell_Mnc);
        this.lengthMap.put("iCell_Lac", Integer.valueOf(4));
        this.fieldMap.put("iCell_Lac", this.iCell_Lac);
        this.lengthMap.put("usCell_ID", Integer.valueOf(2));
        this.fieldMap.put("usCell_ID", this.usCell_ID);
        this.lengthMap.put("iSignal_Strength", Integer.valueOf(4));
        this.fieldMap.put("iSignal_Strength", this.iSignal_Strength);
        this.lengthMap.put("iRAT", Integer.valueOf(4));
        this.fieldMap.put("iRAT", this.iRAT);
        this.lengthMap.put("iChannel_Number", Integer.valueOf(4));
        this.fieldMap.put("iChannel_Number", this.iChannel_Number);
        this.lengthMap.put("iPhysical_Identity", Integer.valueOf(4));
        this.fieldMap.put("iPhysical_Identity", this.iPhysical_Identity);
        this.enSubEventId.setValue("CellInfo");
    }
}
