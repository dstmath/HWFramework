package jcifs.smb;

import java.io.IOException;

class NtTransQuerySecurityDescResponse extends SmbComNtTransactionResponse {
    SecurityDescriptor securityDescriptor;

    NtTransQuerySecurityDescResponse() {
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
        this.length = ServerMessageBlock.readInt4(buffer, bufferIndex);
        return 4;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
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
