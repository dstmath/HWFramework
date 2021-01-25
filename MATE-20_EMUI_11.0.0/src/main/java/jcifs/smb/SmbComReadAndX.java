package jcifs.smb;

import jcifs.Config;

/* access modifiers changed from: package-private */
public class SmbComReadAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = Config.getInt("jcifs.smb.client.ReadAndX.Close", 1);
    private int fid;
    int maxCount;
    int minCount;
    private long offset;
    private int openTimeout = -1;
    int remaining;

    SmbComReadAndX() {
        super(null);
        this.command = 46;
    }

    SmbComReadAndX(int fid2, long offset2, int maxCount2, ServerMessageBlock andx) {
        super(andx);
        this.fid = fid2;
        this.offset = offset2;
        this.minCount = maxCount2;
        this.maxCount = maxCount2;
        this.command = 46;
    }

    /* access modifiers changed from: package-private */
    public void setParam(int fid2, long offset2, int maxCount2) {
        this.fid = fid2;
        this.offset = offset2;
        this.minCount = maxCount2;
        this.maxCount = maxCount2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.AndXServerMessageBlock
    public int getBatchLimit(byte command) {
        if (command == 4) {
            return BATCH_LIMIT;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt4(this.offset, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 4;
        writeInt2((long) this.maxCount, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        writeInt2((long) this.minCount, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 2;
        writeInt4((long) this.openTimeout, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        writeInt2((long) this.remaining, dst, dstIndex6);
        int dstIndex7 = dstIndex6 + 2;
        writeInt4(this.offset >> 32, dst, dstIndex7);
        return (dstIndex7 + 4) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
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
        return new String("SmbComReadAndX[" + super.toString() + ",fid=" + this.fid + ",offset=" + this.offset + ",maxCount=" + this.maxCount + ",minCount=" + this.minCount + ",openTimeout=" + this.openTimeout + ",remaining=" + this.remaining + ",offset=" + this.offset + "]");
    }
}
