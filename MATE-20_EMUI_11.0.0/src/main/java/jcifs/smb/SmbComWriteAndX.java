package jcifs.smb;

import jcifs.Config;

/* access modifiers changed from: package-private */
public class SmbComWriteAndX extends AndXServerMessageBlock {
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
    @Override // jcifs.smb.AndXServerMessageBlock
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
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        this.dataOffset = (dstIndex - this.headerStart) + 26;
        this.pad = (this.dataOffset - this.headerStart) % 4;
        this.pad = this.pad == 0 ? 0 : 4 - this.pad;
        this.dataOffset += this.pad;
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt4(this.offset, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 4;
        for (int i = 0; i < 4; i++) {
            dstIndex3++;
            dst[dstIndex3] = -1;
        }
        writeInt2((long) this.writeMode, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        writeInt2((long) this.remaining, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 2;
        int dstIndex6 = dstIndex5 + 1;
        dst[dstIndex5] = 0;
        int dstIndex7 = dstIndex6 + 1;
        dst[dstIndex6] = 0;
        writeInt2((long) this.dataLength, dst, dstIndex7);
        int dstIndex8 = dstIndex7 + 2;
        writeInt2((long) this.dataOffset, dst, dstIndex8);
        int dstIndex9 = dstIndex8 + 2;
        writeInt4(this.offset >> 32, dst, dstIndex9);
        return (dstIndex9 + 4) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        while (true) {
            int i = this.pad;
            this.pad = i - 1;
            if (i > 0) {
                dst[dstIndex] = -18;
                dstIndex++;
            } else {
                System.arraycopy(this.b, this.off, dst, dstIndex, this.dataLength);
                return (dstIndex + this.dataLength) - dstIndex;
            }
        }
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

    @Override // jcifs.smb.AndXServerMessageBlock, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComWriteAndX[" + super.toString() + ",fid=" + this.fid + ",offset=" + this.offset + ",writeMode=" + this.writeMode + ",remaining=" + this.remaining + ",dataLength=" + this.dataLength + ",dataOffset=" + this.dataOffset + "]");
    }
}
