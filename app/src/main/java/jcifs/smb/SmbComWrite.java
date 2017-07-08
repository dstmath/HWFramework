package jcifs.smb;

class SmbComWrite extends ServerMessageBlock {
    private byte[] b;
    private int count;
    private int fid;
    private int off;
    private int offset;
    private int remaining;

    SmbComWrite() {
        this.command = (byte) 11;
    }

    SmbComWrite(int fid, int offset, int remaining, byte[] b, int off, int len) {
        this.fid = fid;
        this.count = len;
        this.offset = offset;
        this.remaining = remaining;
        this.b = b;
        this.off = off;
        this.command = (byte) 11;
    }

    void setParam(int fid, long offset, int remaining, byte[] b, int off, int len) {
        this.fid = fid;
        this.offset = (int) (4294967295L & offset);
        this.remaining = remaining;
        this.b = b;
        this.off = off;
        this.count = len;
        this.digest = null;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.count, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.offset, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.remaining, dst, dstIndex);
        return (dstIndex + 2) - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 1;
        ServerMessageBlock.writeInt2((long) this.count, dst, dstIndex2);
        dstIndex = dstIndex2 + 2;
        System.arraycopy(this.b, this.off, dst, dstIndex, this.count);
        return (dstIndex + this.count) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComWrite[" + super.toString() + ",fid=" + this.fid + ",count=" + this.count + ",offset=" + this.offset + ",remaining=" + this.remaining + "]");
    }
}
