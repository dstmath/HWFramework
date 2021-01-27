package com.huawei.coauth.auth.authentity;

public class CoAuthPairGroupEntity {
    private String dstDid;
    private int dstModule;
    private byte[] groupId;
    private String srcDid;
    private int srcModule;
    private int version;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDstModule(int dstModule2) {
        this.dstModule = dstModule2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGroupId(byte[] groupId2) {
        this.groupId = groupId2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVersion(int version2) {
        this.version = version2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSrcDid(String srcDid2) {
        this.srcDid = srcDid2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSrcModule(int srcModule2) {
        this.srcModule = srcModule2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDstDid(String dstDid2) {
        this.dstDid = dstDid2;
    }

    public int getDstModule() {
        return this.dstModule;
    }

    public byte[] getGroupId() {
        byte[] bArr = this.groupId;
        if (bArr == null || bArr.length == 0) {
            return new byte[0];
        }
        byte[] out = new byte[bArr.length];
        System.arraycopy(bArr, 0, out, 0, bArr.length);
        return out;
    }

    public int getVersion() {
        return this.version;
    }

    public String getSrcDid() {
        return this.srcDid;
    }

    public int getSrcModule() {
        return this.srcModule;
    }

    public String getDstDid() {
        return this.dstDid;
    }

    public static class Builder {
        private String dstDid;
        private int dstModule;
        private byte[] groupId;
        private String srcDid;
        private int srcModule;
        private int version;

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

        public Builder setVersion(int version2) {
            this.version = version2;
            return this;
        }

        public Builder setSrcDid(String srcDid2) {
            this.srcDid = srcDid2;
            return this;
        }

        public Builder setSrcModule(int srcModule2) {
            this.srcModule = srcModule2;
            return this;
        }

        public Builder setDstDid(String dstDid2) {
            this.dstDid = dstDid2;
            return this;
        }

        public Builder setDstModule(int dstModule2) {
            this.dstModule = dstModule2;
            return this;
        }

        public CoAuthPairGroupEntity build() {
            CoAuthPairGroupEntity coAuthPairGroupEntity = new CoAuthPairGroupEntity();
            coAuthPairGroupEntity.setGroupId(this.groupId);
            coAuthPairGroupEntity.setVersion(this.version);
            coAuthPairGroupEntity.setSrcDid(this.srcDid);
            coAuthPairGroupEntity.setSrcModule(this.srcModule);
            coAuthPairGroupEntity.setDstDid(this.dstDid);
            coAuthPairGroupEntity.setDstModule(this.dstModule);
            return coAuthPairGroupEntity;
        }
    }
}
