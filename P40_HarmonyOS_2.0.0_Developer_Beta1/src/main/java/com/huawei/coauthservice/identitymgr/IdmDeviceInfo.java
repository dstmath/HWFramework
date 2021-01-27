package com.huawei.coauthservice.identitymgr;

import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;

public class IdmDeviceInfo {
    private String deviceId;
    private String ip;
    private IdmLinkType linkType;

    public IdmLinkType getIdmLinkType() {
        return this.linkType;
    }

    public void setIdmLinkType(IdmLinkType idmLinkType) {
        this.linkType = idmLinkType;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip2) {
        this.ip = ip2;
    }

    public String toString() {
        return "IdmDeviceInfo{linkType=" + this.linkType + ", deviceId='" + HwDeviceUtils.maskDeviceId(this.deviceId) + "', ip='" + HwDeviceUtils.maskDeviceIp(this.ip) + "'}";
    }
}
