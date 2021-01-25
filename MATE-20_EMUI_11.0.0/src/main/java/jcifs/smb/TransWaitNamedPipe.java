package jcifs.smb;

/* access modifiers changed from: package-private */
public class TransWaitNamedPipe extends SmbComTransaction {
    TransWaitNamedPipe(String pipeName) {
        this.name = pipeName;
        this.command = 37;
        this.subCommand = 83;
        this.timeout = -1;
        this.maxParameterCount = 0;
        this.maxDataCount = 0;
        this.maxSetupCount = 0;
        this.setupCount = 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = 0;
        int i = dstIndex4 + 1;
        dst[dstIndex4] = 0;
        return 4;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    @Override // jcifs.smb.SmbComTransaction, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("TransWaitNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
