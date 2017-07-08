package com.huawei.device.connectivitychrlog;

import huawei.android.provider.HwSettings.System;

public class CSegEVENT_WIFI_HAL_DRIVER_EXCEPTION_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info;
    public ENCEventId enEventId;
    public ENCWifiHalDriverExceptionReason enWifiHalDriverExceptionReason;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_WIFI_HAL_DRIVER_EXCEPTION_EX() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enWifiHalDriverExceptionReason = new ENCWifiHalDriverExceptionReason();
        this.aucExt_info = new LogByteArray(System.EASYWAKE_ENABLE_FLAG_MASK);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiHalDriverExceptionReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiHalDriverExceptionReason", this.enWifiHalDriverExceptionReason);
        this.lengthMap.put("aucExt_info", Integer.valueOf(System.EASYWAKE_ENABLE_FLAG_MASK));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("WIFI_HAL_DRIVER_EXCEPTION_EX");
        this.usLen.setValue(getTotalLen());
    }
}
