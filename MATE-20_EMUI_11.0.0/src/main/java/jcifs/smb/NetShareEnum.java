package jcifs.smb;

import java.io.UnsupportedEncodingException;

/* access modifiers changed from: package-private */
public class NetShareEnum extends SmbComTransaction {
    private static final String DESCR = "WrLeh\u0000B13BWz\u0000";

    NetShareEnum() {
        this.command = 37;
        this.subCommand = 0;
        this.name = new String("\\PIPE\\LANMAN");
        this.maxParameterCount = 8;
        this.maxSetupCount = 0;
        this.setupCount = 0;
        this.timeout = 5000;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        try {
            byte[] descr = DESCR.getBytes("ASCII");
            writeInt2(0, dst, dstIndex);
            int dstIndex2 = dstIndex + 2;
            System.arraycopy(descr, 0, dst, dstIndex2, descr.length);
            int dstIndex3 = dstIndex2 + descr.length;
            writeInt2(1, dst, dstIndex3);
            int dstIndex4 = dstIndex3 + 2;
            writeInt2((long) this.maxDataCount, dst, dstIndex4);
            return (dstIndex4 + 2) - dstIndex;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
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
        return new String("NetShareEnum[" + super.toString() + "]");
    }
}
