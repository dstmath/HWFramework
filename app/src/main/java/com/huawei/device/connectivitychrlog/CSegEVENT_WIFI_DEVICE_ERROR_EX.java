package com.huawei.device.connectivitychrlog;

import huawei.android.provider.HwSettings.System;

public class CSegEVENT_WIFI_DEVICE_ERROR_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info;
    public ENCEventId enEventId;
    public ENCWifiDeviceErrorReason enWifiDeviceErrorReason;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;

    public CSegEVENT_WIFI_DEVICE_ERROR_EX() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.enWifiDeviceErrorReason = new ENCWifiDeviceErrorReason();
        this.aucExt_info = new LogByteArray(System.EASYWAKE_ENABLE_FLAG_MASK);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiDeviceErrorReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiDeviceErrorReason", this.enWifiDeviceErrorReason);
        this.lengthMap.put("aucExt_info", Integer.valueOf(System.EASYWAKE_ENABLE_FLAG_MASK));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("WIFI_DEVICE_ERROR_EX");
        this.usLen.setValue(getTotalLen());
    }
}
