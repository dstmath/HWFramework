package jcifs.smb;

import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComDelete extends ServerMessageBlock {
    private int searchAttributes = 6;

    SmbComDelete(String fileName) {
        this.path = fileName;
        this.command = 6;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.searchAttributes, dst, dstIndex);
        return 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = 4;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComDelete[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",fileName=" + this.path + "]");
    }
}
