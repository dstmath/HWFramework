package com.huawei.coauthservice.identitymgr;

import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;

public class IdmGroupInfo {
    private String adminId;
    private String groupId;

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId2) {
        this.groupId = groupId2;
    }

    public String getAdminId() {
        return this.adminId;
    }

    public void setAdminId(String adminId2) {
        this.adminId = adminId2;
    }

    public String toString() {
        return "IdmGroupInfo{groupId='" + HwDeviceUtils.maskString(this.groupId) + "', adminId='" + this.adminId + "'}";
    }
}
