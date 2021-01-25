package jcifs.smb;

import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class NtTransQuerySecurityDesc extends SmbComNtTransaction {
    int fid;
    int securityInformation;

    NtTransQuerySecurityDesc(int fid2, int securityInformation2) {
        this.fid = fid2;
        this.securityInformation = securityInformation2;
        this.command = -96;
        this.function = 6;
        this.setupCount = 0;
        this.totalDataCount = 0;
        this.maxParameterCount = 4;
        this.maxDataCount = 32768;
        this.maxSetupCount = 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = 0;
        writeInt4((long) this.securityInformation, dst, dstIndex4);
        return (dstIndex4 + 4) - dstIndex;
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
        return new String("NtTransQuerySecurityDesc[" + super.toString() + ",fid=0x" + Hexdump.toHexString(this.fid, 4) + ",securityInformation=0x" + Hexdump.toHexString(this.securityInformation, 8) + "]");
    }
}
