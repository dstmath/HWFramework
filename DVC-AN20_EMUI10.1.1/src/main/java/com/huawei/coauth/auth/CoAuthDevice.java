package com.huawei.coauth.auth;

public class CoAuthDevice {
    private String deviceId;
    private String ip;
    private String port;

    private CoAuthDevice(String deviceId2, String ip2, String port2) {
        this.deviceId = deviceId2;
        this.ip = ip2;
        this.port = port2;
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

    public static class Builder {
        private String deviceId;
        private String ip;
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

        public CoAuthDevice build() {
            return new CoAuthDevice(this.deviceId, this.ip, this.port);
        }
    }
}
