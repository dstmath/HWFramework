package jcifs.smb;

import jcifs.util.Hexdump;

class SmbComRename extends ServerMessageBlock {
    private String newFileName;
    private String oldFileName;
    private int searchAttributes;

    SmbComRename(String oldFileName, String newFileName) {
        this.command = (byte) 7;
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
        this.searchAttributes = 22;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        return 2;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 4;
        dstIndex = dstIndex2 + writeString(this.oldFileName, dst, dstIndex2);
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 4;
        if (this.useUnicode) {
            dstIndex = dstIndex2 + 1;
            dst[dstIndex2] = (byte) 0;
        } else {
            dstIndex = dstIndex2;
        }
        return (dstIndex + writeString(this.newFileName, dst, dstIndex)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComRename[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",oldFileName=" + this.oldFileName + ",newFileName=" + this.newFileName + "]");
    }
}
