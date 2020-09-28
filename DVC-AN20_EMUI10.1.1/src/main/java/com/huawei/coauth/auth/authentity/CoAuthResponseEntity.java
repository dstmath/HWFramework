package com.huawei.coauth.auth.authentity;

import com.huawei.coauth.auth.authmsg.CoAuthOperationType;

public class CoAuthResponseEntity {
    private CoAuthOperationType coAuthOperationType;
    private byte[] groupId;
    private int resultCode;
    private long sessionId;

    private CoAuthResponseEntity() {
    }

    private CoAuthResponseEntity(long sessionId2, int resultCode2, CoAuthOperationType coAuthOperationType2) {
        this.sessionId = sessionId2;
        this.resultCode = resultCode2;
        this.coAuthOperationType = coAuthOperationType2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCoAuthOperationType(CoAuthOperationType coAuthOperationType2) {
        this.coAuthOperationType = coAuthOperationType2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGroupId(byte[] groupId2) {
        this.groupId = groupId2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSessionId(long sessionId2) {
        this.sessionId = sessionId2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public CoAuthOperationType getCoAuthOperationType() {
        return this.coAuthOperationType;
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

    public long getSessionId() {
        return this.sessionId;
    }

    public int getResultCode() {
        return this.resultCode;
    }

    public static class Builder {
        private CoAuthOperationType coAuthOperationType;
        private byte[] groupId;
        private int resultCode;
        private long sessionId;

        public Builder setCoAuthOperationType(CoAuthOperationType coAuthOperationType2) {
            this.coAuthOperationType = coAuthOperationType2;
            return this;
        }

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

        public Builder setSessionId(long sessionId2) {
            this.sessionId = sessionId2;
            return this;
        }

        public Builder setResultCode(int resultCode2) {
            this.resultCode = resultCode2;
            return this;
        }

        public CoAuthResponseEntity build() {
            CoAuthResponseEntity coAuthResponseEntity = new CoAuthResponseEntity();
            coAuthResponseEntity.setSessionId(this.sessionId);
            coAuthResponseEntity.setGroupId(this.groupId);
            coAuthResponseEntity.setResultCode(this.resultCode);
            coAuthResponseEntity.setCoAuthOperationType(this.coAuthOperationType);
            return coAuthResponseEntity;
        }
    }
}
