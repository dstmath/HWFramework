package com.huawei.coauth.auth.authentity;

public class CoAuthHeaderEntity {
    private String dstDeviceIp;
    private String dstDevicePort;
    private String dstDid;
    private int dstModule;
    private String srcDid;
    private int srcModule;
    private int version;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVersion(int version2) {
        this.version = version2;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDstModule(int dstModule2) {
        this.dstModule = dstModule2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDstDeviceIp(String dstDeviceIp2) {
        this.dstDeviceIp = dstDeviceIp2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDstDevicePort(String dstDevicePort2) {
        this.dstDevicePort = dstDevicePort2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSrcDid(String srcDid2) {
        this.srcDid = srcDid2;
    }

    public int getVersion() {
        return this.version;
    }

    public int getSrcModule() {
        return this.srcModule;
    }

    public String getSrcDid() {
        return this.srcDid;
    }

    public int getDstModule() {
        return this.dstModule;
    }

    public String getDstDid() {
        return this.dstDid;
    }

    public String getDstDevicePort() {
        return this.dstDevicePort;
    }

    public String getDstDeviceIp() {
        return this.dstDeviceIp;
    }

    public static class Builder {
        private String dstDeviceIp;
        private String dstDevicePort;
        private String dstDid;
        private int dstModule;
        private String srcDid;
        private int srcModule;
        private int version;

        public Builder setVersion(int version2) {
            this.version = version2;
            return this;
        }

        public Builder setSrcModule(int srcModule2) {
            this.srcModule = srcModule2;
            return this;
        }

        public Builder setSrcDid(String srcDid2) {
            this.srcDid = srcDid2;
            return this;
        }

        public Builder setDstModule(int dstModule2) {
            this.dstModule = dstModule2;
            return this;
        }

        public Builder setDstDid(String dstDid2) {
            this.dstDid = dstDid2;
            return this;
        }

        public Builder setDstDevicePort(String dstDevicePort2) {
            this.dstDevicePort = dstDevicePort2;
            return this;
        }

        public Builder setDstDeviceIp(String dstDeviceIp2) {
            this.dstDeviceIp = dstDeviceIp2;
            return this;
        }

        public CoAuthHeaderEntity build() {
            CoAuthHeaderEntity coAuthHeaderEntity = new CoAuthHeaderEntity();
            coAuthHeaderEntity.setVersion(this.version);
            coAuthHeaderEntity.setSrcDid(this.srcDid);
            coAuthHeaderEntity.setSrcModule(this.srcModule);
            coAuthHeaderEntity.setDstDid(this.dstDid);
            coAuthHeaderEntity.setDstModule(this.dstModule);
            coAuthHeaderEntity.setDstDeviceIp(this.dstDeviceIp);
            coAuthHeaderEntity.setDstDevicePort(this.dstDevicePort);
            return coAuthHeaderEntity;
        }
    }
}
