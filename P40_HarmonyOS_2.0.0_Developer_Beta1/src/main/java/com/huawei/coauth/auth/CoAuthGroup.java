package com.huawei.coauth.auth;

public class CoAuthGroup {
    private byte[] groupId;

    CoAuthGroup(byte[] groupId2) {
        if (groupId2 == null || groupId2.length == 0) {
            this.groupId = new byte[0];
            return;
        }
        byte[] out = new byte[groupId2.length];
        System.arraycopy(groupId2, 0, out, 0, groupId2.length);
        this.groupId = out;
    }

    public String getGroupId() {
        byte[] bArr = this.groupId;
        if (bArr == null) {
            return CoAuthUtil.INVALID_GID;
        }
        return CoAuthUtil.bytesToHexString(bArr).get();
    }
}
