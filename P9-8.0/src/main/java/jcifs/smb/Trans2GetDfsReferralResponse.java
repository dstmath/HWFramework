package jcifs.smb;

class Trans2GetDfsReferralResponse extends SmbComTransactionResponse {
    int flags;
    int numReferrals;
    int pathConsumed;
    Referral[] referrals;

    class Referral {
        private String altPath;
        private int altPathOffset;
        private int flags;
        String node = null;
        private int nodeOffset;
        String path = null;
        private int pathOffset;
        private int proximity;
        private int serverType;
        private int size;
        int ttl;
        private int version;

        Referral() {
        }

        int readWireFormat(byte[] buffer, int bufferIndex, int len) {
            boolean z = true;
            int start = bufferIndex;
            this.version = ServerMessageBlock.readInt2(buffer, bufferIndex);
            if (this.version == 3 || this.version == 1) {
                bufferIndex += 2;
                this.size = ServerMessageBlock.readInt2(buffer, bufferIndex);
                bufferIndex += 2;
                this.serverType = ServerMessageBlock.readInt2(buffer, bufferIndex);
                bufferIndex += 2;
                this.flags = ServerMessageBlock.readInt2(buffer, bufferIndex);
                bufferIndex += 2;
                Trans2GetDfsReferralResponse trans2GetDfsReferralResponse;
                if (this.version == 3) {
                    this.proximity = ServerMessageBlock.readInt2(buffer, bufferIndex);
                    bufferIndex += 2;
                    this.ttl = ServerMessageBlock.readInt2(buffer, bufferIndex);
                    bufferIndex += 2;
                    this.pathOffset = ServerMessageBlock.readInt2(buffer, bufferIndex);
                    bufferIndex += 2;
                    this.altPathOffset = ServerMessageBlock.readInt2(buffer, bufferIndex);
                    bufferIndex += 2;
                    this.nodeOffset = ServerMessageBlock.readInt2(buffer, bufferIndex);
                    bufferIndex += 2;
                    this.path = Trans2GetDfsReferralResponse.this.readString(buffer, start + this.pathOffset, len, (Trans2GetDfsReferralResponse.this.flags2 & 32768) != 0);
                    if (this.nodeOffset > 0) {
                        trans2GetDfsReferralResponse = Trans2GetDfsReferralResponse.this;
                        int i = this.nodeOffset + start;
                        if ((Trans2GetDfsReferralResponse.this.flags2 & 32768) == 0) {
                            z = false;
                        }
                        this.node = trans2GetDfsReferralResponse.readString(buffer, i, len, z);
                    }
                } else if (this.version == 1) {
                    trans2GetDfsReferralResponse = Trans2GetDfsReferralResponse.this;
                    if ((Trans2GetDfsReferralResponse.this.flags2 & 32768) == 0) {
                        z = false;
                    }
                    this.node = trans2GetDfsReferralResponse.readString(buffer, bufferIndex, len, z);
                }
                return this.size;
            }
            throw new RuntimeException("Version " + this.version + " referral not supported. Please report this to jcifs at samba dot org.");
        }

        public String toString() {
            return new String("Referral[version=" + this.version + ",size=" + this.size + ",serverType=" + this.serverType + ",flags=" + this.flags + ",proximity=" + this.proximity + ",ttl=" + this.ttl + ",pathOffset=" + this.pathOffset + ",altPathOffset=" + this.altPathOffset + ",nodeOffset=" + this.nodeOffset + ",path=" + this.path + ",altPath=" + this.altPath + ",node=" + this.node + "]");
        }
    }

    Trans2GetDfsReferralResponse() {
        this.subCommand = (byte) 16;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        int start = bufferIndex;
        this.pathConsumed = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        if ((this.flags2 & 32768) != 0) {
            this.pathConsumed /= 2;
        }
        this.numReferrals = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.flags = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 4;
        this.referrals = new Referral[this.numReferrals];
        for (int ri = 0; ri < this.numReferrals; ri++) {
            this.referrals[ri] = new Referral();
            bufferIndex += this.referrals[ri].readWireFormat(buffer, bufferIndex, len);
        }
        return bufferIndex - start;
    }

    public String toString() {
        return new String("Trans2GetDfsReferralResponse[" + super.toString() + ",pathConsumed=" + this.pathConsumed + ",numReferrals=" + this.numReferrals + ",flags=" + this.flags + "]");
    }
}
