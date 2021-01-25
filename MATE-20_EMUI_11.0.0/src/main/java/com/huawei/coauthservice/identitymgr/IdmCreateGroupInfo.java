package com.huawei.coauthservice.identitymgr;

import java.util.List;

public class IdmCreateGroupInfo {
    private String moduleName;
    private boolean overwrite = true;
    private List<IdmDeviceInfo> peerDeviceInfoList;

    public List<IdmDeviceInfo> getPeerDeviceInfoList() {
        return this.peerDeviceInfoList;
    }

    public void setPeerDeviceInfoList(List<IdmDeviceInfo> peerDeviceInfoList2) {
        this.peerDeviceInfoList = peerDeviceInfoList2;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void setModuleName(String moduleName2) {
        this.moduleName = moduleName2;
    }

    public boolean isOverwrite() {
        return this.overwrite;
    }

    public void setOverwrite(boolean overwrite2) {
        this.overwrite = overwrite2;
    }

    public String toString() {
        return "IdmCreateGroupInfo{moduleName='" + this.moduleName + "', overwrite=" + this.overwrite + ", peerDeviceInfoList=" + this.peerDeviceInfoList + '}';
    }
}
