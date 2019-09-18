package jcifs.smb;

import jcifs.Config;

class SmbComWriteAndX extends AndXServerMessageBlock {
    private static final int CLOSE_BATCH_LIMIT = Config.getInt("jcifs.smb.client.WriteAndX.Close", 1);
    private static final int READ_ANDX_BATCH_LIMIT = Config.getInt("jcifs.smb.client.WriteAndX.ReadAndX", 1);
    private byte[] b;
    private int dataLength;
    private int dataOffset;
    private int fid;
    private int off;
    private long offset;
    private int pad;
    private int remaining;
    int writeMode;

    SmbComWriteAndX() {
        super(null);
        this.command = 47;
    }

    SmbComWriteAndX(int fid2, long offset2, int remaining2, byte[] b2, int off2, int len, ServerMessageBlock andx) {
        super(andx);
        this.fid = fid2;
        this.offset = offset2;
        this.remaining = remaining2;
        this.b = b2;
        this.off = off2;
        this.dataLength = len;
        this.command = 47;
    }

    /* access modifiers changed from: package-private */
    public void setParam(int fid2, long offset2, int remaining2, byte[] b2, int off2, int len) {
        this.fid = fid2;
        this.offset = offset2;
        this.remaining = remaining2;
        this.b = b2;
        this.off = off2;
        this.dataLength = len;
        this.digest = null;
    }

    /* access modifiers changed from: package-private */
    public int getBatchLimit(byte command) {
        if (command == 46) {
            return READ_ANDX_BATCH_LIMIT;
        }
        if (command == 4) {
            return CLOSE_BATCH_LIMIT;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        this.dataOffset = (dstIndex - this.headerStart) + 26;
        this.pad = (this.dataOffset - this.headerStart) % 4;
        this.pad = this.pad == 0 ? 0 : 4 - this.pad;
        this.dataOffset += this.pad;
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt4(this.offset, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 4;
        int i = 0;
        while (true) {
            int dstIndex4 = dstIndex3;
            if (i < 4) {
                dstIndex3 = dstIndex4 + 1;
                dst[dstIndex4] = -1;
                i++;
            } else {
                writeInt2((long) this.writeMode, dst, dstIndex4);
                int dstIndex5 = dstIndex4 + 2;
                writeInt2((long) this.remaining, dst, dstIndex5);
                int dstIndex6 = dstIndex5 + 2;
                int dstIndex7 = dstIndex6 + 1;
                dst[dstIndex6] = 0;
                int dstIndex8 = dstIndex7 + 1;
                dst[dstIndex7] = 0;
                writeInt2((long) this.dataLength, dst, dstIndex8);
                int dstIndex9 = dstIndex8 + 2;
                writeInt2((long) this.dataOffset, dst, dstIndex9);
                int dstIndex10 = dstIndex9 + 2;
                writeInt4(this.offset >> 32, dst, dstIndex10);
                return (dstIndex10 + 4) - start;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        while (true) {
            int i = this.pad;
            this.pad = i - 1;
            if (i > 0) {
                dst[dstIndex] = -18;
                dstIndex++;
            } else {
                System.arraycopy(this.b, this.off, dst, dstIndex, this.dataLength);
                return (dstIndex + this.dataLength) - start;
            }
        }
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
        return new String("SmbComWriteAndX[" + super.toString() + ",fid=" + this.fid + ",offset=" + this.offset + ",writeMode=" + this.writeMode + ",remaining=" + this.remaining + ",dataLength=" + this.dataLength + ",dataOffset=" + this.dataOffset + "]");
    }
}
