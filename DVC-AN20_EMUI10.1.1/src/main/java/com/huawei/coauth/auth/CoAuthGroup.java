package com.huawei.coauth.auth;

public class CoAuthGroup {
    private byte[] groupId;

    CoAuthGroup(byte[] groupId2) {
        this.groupId = groupId2;
    }

    public String getGroupId() {
        return CoAuthUtil.bytesToHexString(this.groupId).get();
    }
}
