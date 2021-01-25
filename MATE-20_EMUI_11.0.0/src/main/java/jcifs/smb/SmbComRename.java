package jcifs.smb;

import jcifs.util.Hexdump;

class SmbComRename extends ServerMessageBlock {
    private String newFileName;
    private String oldFileName;
    private int searchAttributes = 22;

    SmbComRename(String oldFileName2, String newFileName2) {
        this.command = 7;
        this.oldFileName = oldFileName2;
        this.newFileName = newFileName2;
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
        int dstIndex2;
        int dstIndex3 = dstIndex + 1;
        dst[dstIndex] = 4;
        int dstIndex4 = dstIndex3 + writeString(this.oldFileName, dst, dstIndex3);
        int dstIndex5 = dstIndex4 + 1;
        dst[dstIndex4] = 4;
        if (this.useUnicode) {
            dstIndex2 = dstIndex5 + 1;
            dst[dstIndex5] = 0;
        } else {
            dstIndex2 = dstIndex5;
        }
        return (dstIndex2 + writeString(this.newFileName, dst, dstIndex2)) - dstIndex;
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
        return new String("SmbComRename[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",oldFileName=" + this.oldFileName + ",newFileName=" + this.newFileName + "]");
    }
}
