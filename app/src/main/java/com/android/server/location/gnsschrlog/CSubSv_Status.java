package com.android.server.location.gnsschrlog;

import huawei.com.android.server.policy.HwGlobalActionsData;

public class CSubSv_Status extends ChrLogBaseEventModel {
    public ENCSubEventId enSubEventId;
    public LogInt iSvCount;
    public LogInt iUsedSvCount;
    public LogString strSvInfo;

    public CSubSv_Status() {
        this.enSubEventId = new ENCSubEventId();
        this.iSvCount = new LogInt();
        this.iUsedSvCount = new LogInt();
        this.strSvInfo = new LogString(HwGlobalActionsData.FLAG_SILENTMODE_SILENT);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iSvCount", Integer.valueOf(4));
        this.fieldMap.put("iSvCount", this.iSvCount);
        this.lengthMap.put("iUsedSvCount", Integer.valueOf(4));
        this.fieldMap.put("iUsedSvCount", this.iUsedSvCount);
        this.lengthMap.put("strSvInfo", Integer.valueOf(HwGlobalActionsData.FLAG_SILENTMODE_SILENT));
        this.fieldMap.put("strSvInfo", this.strSvInfo);
        this.enSubEventId.setValue("Sv_Status");
    }
}
