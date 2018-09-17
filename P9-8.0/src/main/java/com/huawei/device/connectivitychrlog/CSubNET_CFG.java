package com.huawei.device.connectivitychrlog;

public class CSubNET_CFG extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iIpMask = new LogInt();
    public LogInt iIp_Type = new LogInt();
    public LogString strProxySettingInfo = new LogString(64);
    public LogByte ucProxySettings = new LogByte();
    public LogByte ucVPN = new LogByte();

    public CSubNET_CFG() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucProxySettings", Integer.valueOf(1));
        this.fieldMap.put("ucProxySettings", this.ucProxySettings);
        this.lengthMap.put("strProxySettingInfo", Integer.valueOf(64));
        this.fieldMap.put("strProxySettingInfo", this.strProxySettingInfo);
        this.lengthMap.put("ucVPN", Integer.valueOf(1));
        this.fieldMap.put("ucVPN", this.ucVPN);
        this.lengthMap.put("iIpMask", Integer.valueOf(4));
        this.fieldMap.put("iIpMask", this.iIpMask);
        this.lengthMap.put("iIp_Type", Integer.valueOf(4));
        this.fieldMap.put("iIp_Type", this.iIp_Type);
        this.enSubEventId.setValue("NET_CFG");
    }
}
