package com.huawei.coauth.auth;

public class CoAuthContext {
    private CoAuthType authType;
    private long challenge;
    private boolean coAuthBegin;
    private CoAuthGroup coAuthGroup;
    private String sensorDeviceId;
    private long sessionId;
    private String verifyDeviceId;

    private CoAuthContext() {
        this.challenge = 0;
        this.coAuthBegin = false;
    }

    public CoAuthGroup getCoAuthGroup() {
        return this.coAuthGroup;
    }

    public long getChallenge() {
        return this.challenge;
    }

    public CoAuthType getAuthType() {
        return this.authType;
    }

    public String getSensorDeviceId() {
        return this.sensorDeviceId;
    }

    public String getVerifyDeviceId() {
        return this.verifyDeviceId;
    }

    /* access modifiers changed from: package-private */
    public long getSessionId() {
        return this.sessionId;
    }

    /* access modifiers changed from: package-private */
    public boolean isCoAuthBegin() {
        return this.coAuthBegin;
    }

    /* access modifiers changed from: package-private */
    public void setCoAuthBegin(boolean coAuthBegin2) {
        this.coAuthBegin = coAuthBegin2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setSessionId(long sessionId2) {
        this.sessionId = sessionId2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setCoAuthGroup(CoAuthGroup coAuthGroup2) {
        this.coAuthGroup = coAuthGroup2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setChallenge(long challenge2) {
        this.challenge = challenge2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setAuthType(CoAuthType authType2) {
        this.authType = authType2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setSensorDeviceId(String sensorDeviceId2) {
        this.sensorDeviceId = sensorDeviceId2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setVerifyDeviceId(String verifyDeviceId2) {
        this.verifyDeviceId = verifyDeviceId2;
        return this;
    }

    public static class Builder {
        private CoAuthType authType;
        private long challenge;
        private CoAuthGroup coAuthGroup;
        private String sensorDeviceId;
        private String verifyDeviceId;

        public Builder setCoAuthGroup(CoAuthGroup coAuthGroup2) {
            this.coAuthGroup = coAuthGroup2;
            return this;
        }

        public Builder setChallenge(long challenge2) {
            this.challenge = challenge2;
            return this;
        }

        public Builder setAuthType(CoAuthType authType2) {
            this.authType = authType2;
            return this;
        }

        public Builder setSensorDeviceId(String sensorDeviceId2) {
            this.sensorDeviceId = sensorDeviceId2;
            return this;
        }

        public Builder setVerifyDeviceId(String verifyDeviceId2) {
            this.verifyDeviceId = verifyDeviceId2;
            return this;
        }

        public CoAuthContext build() {
            return new CoAuthContext().setAuthType(this.authType).setCoAuthGroup(this.coAuthGroup).setChallenge(this.challenge).setSensorDeviceId(this.sensorDeviceId).setVerifyDeviceId(this.verifyDeviceId).setSessionId(CoAuthUtil.getNewSessionId());
        }
    }
}
