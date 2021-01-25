package com.huawei.coauth.auth;

public class CoAuthDevice {
    private String deviceId;
    private String extraMeta;
    private String ip;
    private int peerLinkMode;
    private int peerLinkType;
    private String port;

    private CoAuthDevice(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getIp() {
        return this.ip;
    }

    public String getPort() {
        return this.port;
    }

    public String getExtraMeta() {
        return this.extraMeta;
    }

    public int getPeerLinkType() {
        return this.peerLinkType;
    }

    public int getPeerLinkMode() {
        return this.peerLinkMode;
    }

    public static class Builder {
        private String deviceId;
        private String extraMeta;
        private String ip;
        private int peerLinkMode;
        private int peerLinkType;
        private String port;

        public Builder setDeviceId(String deviceId2) {
            this.deviceId = deviceId2;
            return this;
        }

        public Builder setIp(String ip2) {
            this.ip = ip2;
            return this;
        }

        public Builder setPort(String port2) {
            this.port = port2;
            return this;
        }

        public Builder setExtraMeta(String extraMeta2) {
            this.extraMeta = extraMeta2;
            return this;
        }

        public Builder setPeerLinkType(int peerLinkType2) {
            this.peerLinkType = peerLinkType2;
            return this;
        }

        public Builder setPeerLinkMode(int peerLinkMode2) {
            this.peerLinkMode = peerLinkMode2;
            return this;
        }

        public CoAuthDevice build() {
            CoAuthDevice device = new CoAuthDevice(this.deviceId);
            device.ip = this.ip;
            device.port = this.port;
            device.extraMeta = this.extraMeta;
            device.peerLinkType = this.peerLinkType;
            device.peerLinkMode = this.peerLinkMode;
            return device;
        }
    }
}
