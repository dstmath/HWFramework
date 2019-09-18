package jcifs.smb;

class SmbComWrite extends ServerMessageBlock {
    private byte[] b;
    private int count;
    private int fid;
    private int off;
    private int offset;
    private int remaining;

    SmbComWrite() {
        this.command = 11;
    }

    SmbComWrite(int fid2, int offset2, int remaining2, byte[] b2, int off2, int len) {
        this.fid = fid2;
        this.count = len;
        this.offset = offset2;
        this.remaining = remaining2;
        this.b = b2;
        this.off = off2;
        this.command = 11;
    }

    /* access modifiers changed from: package-private */
    public void setParam(int fid2, long offset2, int remaining2, byte[] b2, int off2, int len) {
        this.fid = fid2;
        this.offset = (int) (4294967295L & offset2);
        this.remaining = remaining2;
        this.b = b2;
        this.off = off2;
        this.count = len;
        this.digest = null;
    }

    /* access modifiers changed from: package-private */
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt2((long) this.count, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        writeInt4((long) this.offset, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 4;
        writeInt2((long) this.remaining, dst, dstIndex4);
        return (dstIndex4 + 2) - start;
    }

    /* access modifiers changed from: package-private */
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = 1;
        writeInt2((long) this.count, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        System.arraycopy(this.b, this.off, dst, dstIndex3, this.count);
        return (dstIndex3 + this.count) - start;
    }

    /* access modifiers changed from: package-private */
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComWrite[" + super.toString() + ",fid=" + this.fid + ",count=" + this.count + ",offset=" + this.offset + ",remaining=" + this.remaining + "]");
    }
}
