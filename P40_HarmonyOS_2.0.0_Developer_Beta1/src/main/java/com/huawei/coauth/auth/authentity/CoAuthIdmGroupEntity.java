package com.huawei.coauth.auth.authentity;

import com.huawei.coauth.auth.CoAuthDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoAuthIdmGroupEntity extends CoAuthRspBaseEntity {
    private static final int INITIAL_CAPACITY = 1;
    private List<CoAuthDevice> devList = new ArrayList(1);
    private byte[] groupId;
    private int resultCode;

    public byte[] getGroupId() {
        byte[] bArr = this.groupId;
        if (bArr == null || bArr.length == 0) {
            return new byte[0];
        }
        byte[] out = new byte[bArr.length];
        System.arraycopy(bArr, 0, out, 0, bArr.length);
        return out;
    }

    public void setGroupId(byte[] groupId2) {
        if (groupId2 != null) {
            this.groupId = Arrays.copyOf(groupId2, groupId2.length);
        }
    }

    public List<CoAuthDevice> getDevList() {
        return this.devList;
    }

    public void setDevList(List<CoAuthDevice> devList2) {
        this.devList.clear();
        this.devList.addAll(devList2);
    }

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public static class Builder {
        private List<CoAuthDevice> devList = new ArrayList(1);
        private byte[] groupId;
        private int resultCode;

        public Builder setGroupId(byte[] groupId2) {
            if (groupId2 == null || groupId2.length == 0) {
                this.groupId = new byte[0];
            } else {
                byte[] out = new byte[groupId2.length];
                System.arraycopy(groupId2, 0, out, 0, groupId2.length);
                this.groupId = out;
            }
            return this;
        }

        public Builder setDevList(CoAuthDevice devList2) {
            this.devList.add(devList2);
            return this;
        }

        public Builder setResultCode(int resultCode2) {
            this.resultCode = resultCode2;
            return this;
        }

        public CoAuthIdmGroupEntity build() {
            CoAuthIdmGroupEntity coAuthIdmGroupEntity = new CoAuthIdmGroupEntity();
            coAuthIdmGroupEntity.setGroupId(this.groupId);
            coAuthIdmGroupEntity.setDevList(this.devList);
            coAuthIdmGroupEntity.setResultCode(this.resultCode);
            return coAuthIdmGroupEntity;
        }
    }
}
