package jcifs.smb;

import java.io.IOException;

class NtTransQuerySecurityDescResponse extends SmbComNtTransactionResponse {
    SecurityDescriptor securityDescriptor;

    NtTransQuerySecurityDescResponse() {
    }

    /* access modifiers changed from: package-private */
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        this.length = readInt4(buffer, bufferIndex);
        return 4;
    }

    /* access modifiers changed from: package-private */
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        int start = bufferIndex;
        if (this.errorCode != 0) {
            return 4;
        }
        try {
            this.securityDescriptor = new SecurityDescriptor();
            return (bufferIndex + this.securityDescriptor.decode(buffer, bufferIndex, len)) - start;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    public String toString() {
        return new String("NtTransQuerySecurityResponse[" + super.toString() + "]");
    }
}
