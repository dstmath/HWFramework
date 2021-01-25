package jcifs.smb;

import java.io.IOException;

/* access modifiers changed from: package-private */
public class NtTransQuerySecurityDescResponse extends SmbComNtTransactionResponse {
    SecurityDescriptor securityDescriptor;

    NtTransQuerySecurityDescResponse() {
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        this.length = readInt4(buffer, bufferIndex);
        return 4;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        if (this.errorCode != 0) {
            return 4;
        }
        try {
            this.securityDescriptor = new SecurityDescriptor();
            return (bufferIndex + this.securityDescriptor.decode(buffer, bufferIndex, len)) - bufferIndex;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("NtTransQuerySecurityResponse[" + super.toString() + "]");
    }
}
