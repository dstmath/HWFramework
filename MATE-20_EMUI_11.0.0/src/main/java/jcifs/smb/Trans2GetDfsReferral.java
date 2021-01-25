package jcifs.smb;

/* access modifiers changed from: package-private */
public class Trans2GetDfsReferral extends SmbComTransaction {
    private int maxReferralLevel = 3;

    Trans2GetDfsReferral(String filename) {
        this.path = filename;
        this.command = 50;
        this.subCommand = 16;
        this.totalDataCount = 0;
        this.maxParameterCount = 0;
        this.maxDataCount = 4096;
        this.maxSetupCount = 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        int i = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        return 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.maxReferralLevel, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
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
        return new String("Trans2GetDfsReferral[" + super.toString() + ",maxReferralLevel=0x" + this.maxReferralLevel + ",filename=" + this.path + "]");
    }
}
