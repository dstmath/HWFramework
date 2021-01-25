package jcifs.smb;

class TransPeekNamedPipe extends SmbComTransaction {
    private int fid;

    TransPeekNamedPipe(String pipeName, int fid2) {
        this.name = pipeName;
        this.fid = fid2;
        this.command = 37;
        this.subCommand = 35;
        this.timeout = -1;
        this.maxParameterCount = 6;
        this.maxDataCount = 1;
        this.maxSetupCount = 0;
        this.setupCount = 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dst[dstIndex2] = 0;
        writeInt2((long) this.fid, dst, dstIndex2 + 1);
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
        return new String("TransPeekNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
