package com.huawei.coauthservice.identitymgr;

import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;
import java.util.List;

public class IdmDeleteGroupInfo {
    private String groupId;
    private List<IdmDeviceInfo> peerDeviceInfoList;

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId2) {
        this.groupId = groupId2;
    }

    public List<IdmDeviceInfo> getPeerDeviceInfoList() {
        return this.peerDeviceInfoList;
    }

    public void setPeerDeviceInfoList(List<IdmDeviceInfo> peerDeviceInfoList2) {
        this.peerDeviceInfoList = peerDeviceInfoList2;
    }

    public String toString() {
        return "IdmDeleteGroupInfo{groupId='" + HwDeviceUtils.maskString(this.groupId) + "', peerDeviceInfoList=" + this.peerDeviceInfoList + '}';
    }
}
