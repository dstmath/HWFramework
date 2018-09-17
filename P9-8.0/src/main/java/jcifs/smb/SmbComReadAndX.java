package jcifs.smb;

import jcifs.Config;

class SmbComReadAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = Config.getInt("jcifs.smb.client.ReadAndX.Close", 1);
    private int fid;
    int maxCount;
    int minCount;
    private long offset;
    private int openTimeout;
    int remaining;

    SmbComReadAndX() {
        super(null);
        this.command = (byte) 46;
        this.openTimeout = -1;
    }

    SmbComReadAndX(int fid, long offset, int maxCount, ServerMessageBlock andx) {
        super(andx);
        this.fid = fid;
        this.offset = offset;
        this.minCount = maxCount;
        this.maxCount = maxCount;
        this.command = (byte) 46;
        this.openTimeout = -1;
    }

    void setParam(int fid, long offset, int maxCount) {
        this.fid = fid;
        this.offset = offset;
        this.minCount = maxCount;
        this.maxCount = maxCount;
    }

    int getBatchLimit(byte command) {
        return command == (byte) 4 ? BATCH_LIMIT : 0;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4(this.offset, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.maxCount, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.minCount, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.openTimeout, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.remaining, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4(this.offset >> 32, dst, dstIndex);
        return (dstIndex + 4) - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComReadAndX[" + super.toString() + ",fid=" + this.fid + ",offset=" + this.offset + ",maxCount=" + this.maxCount + ",minCount=" + this.minCount + ",openTimeout=" + this.openTimeout + ",remaining=" + this.remaining + ",offset=" + this.offset + "]");
    }
}
