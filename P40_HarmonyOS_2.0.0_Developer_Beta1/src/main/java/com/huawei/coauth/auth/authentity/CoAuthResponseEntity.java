package com.huawei.coauth.auth.authentity;

import com.huawei.coauth.auth.authmsg.CoAuthOperationType;
import java.util.Arrays;

public class CoAuthResponseEntity extends CoAuthRspBaseEntity {
    private byte[] coAuthToken;
    private byte[] groupId;
    private int resultCode;
    private long sessionId;

    private CoAuthResponseEntity() {
    }

    private CoAuthResponseEntity(long sessionId2, int resultCode2, CoAuthOperationType coAuthOperationType) {
        this.sessionId = sessionId2;
        this.resultCode = resultCode2;
        super.setCoAuthOperationType(coAuthOperationType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGroupId(byte[] groupId2) {
        this.groupId = groupId2 == null ? null : Arrays.copyOf(groupId2, groupId2.length);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCoAuthToken(byte[] coAuthToken2) {
        this.coAuthToken = coAuthToken2 == null ? null : Arrays.copyOf(coAuthToken2, coAuthToken2.length);
    }

    public byte[] getCoAuthToken() {
        byte[] bArr = this.coAuthToken;
        return bArr == null ? new byte[0] : Arrays.copyOf(bArr, bArr.length);
    }

    public byte[] getGroupId() {
        byte[] bArr = this.groupId;
        return bArr == null ? new byte[0] : Arrays.copyOf(bArr, bArr.length);
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public int getResultCode() {
        return this.resultCode;
    }

    public static class Builder {
        private CoAuthOperationType coAuthOperationType;
        private byte[] coAuthToken;
        private byte[] groupId;
        private int resultCode;
        private long sessionId;

        public Builder setCoAuthOperationType(CoAuthOperationType coAuthOperationType2) {
            this.coAuthOperationType = coAuthOperationType2;
            return this;
        }

        public Builder setGroupId(byte[] groupId2) {
            this.groupId = groupId2 == null ? new byte[0] : Arrays.copyOf(groupId2, groupId2.length);
            return this;
        }

        public Builder setCoAuthToken(byte[] coAuthToken2) {
            this.coAuthToken = coAuthToken2 == null ? null : Arrays.copyOf(coAuthToken2, coAuthToken2.length);
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
            coAuthResponseEntity.setCoAuthToken(this.coAuthToken);
            return coAuthResponseEntity;
        }
    }
}
