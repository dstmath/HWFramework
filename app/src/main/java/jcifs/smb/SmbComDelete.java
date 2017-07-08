package jcifs.smb;

import jcifs.util.Hexdump;

class SmbComDelete extends ServerMessageBlock {
    private int searchAttributes;

    SmbComDelete(String fileName) {
        this.path = fileName;
        this.command = (byte) 6;
        this.searchAttributes = 6;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        return 2;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 4;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComDelete[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",fileName=" + this.path + "]");
    }
}
