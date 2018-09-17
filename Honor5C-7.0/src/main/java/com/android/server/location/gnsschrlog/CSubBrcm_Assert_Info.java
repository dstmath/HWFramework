package com.android.server.location.gnsschrlog;

import com.android.server.wifipro.WifiProCommonUtils;

public class CSubBrcm_Assert_Info extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogString strAssertInfo;

    public CSubBrcm_Assert_Info() {
        this.enSubEventId = new ENCSubEventId();
        this.strAssertInfo = new LogString(WifiProCommonUtils.HTTP_REACHALBE_HOME);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAssertInfo", Integer.valueOf(WifiProCommonUtils.HTTP_REACHALBE_HOME));
        this.fieldMap.put("strAssertInfo", this.strAssertInfo);
        this.enSubEventId.setValue("Brcm_Assert_Info");
    }
}
