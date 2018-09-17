package jcifs.smb;

class SmbComClose extends ServerMessageBlock {
    private int fid;
    private long lastWriteTime;

    SmbComClose(int fid, long lastWriteTime) {
        this.fid = fid;
        this.lastWriteTime = lastWriteTime;
        this.command = (byte) 4;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        ServerMessageBlock.writeUTime(this.lastWriteTime, dst, dstIndex + 2);
        return 6;
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
        return new String("SmbComClose[" + super.toString() + ",fid=" + this.fid + ",lastWriteTime=" + this.lastWriteTime + "]");
    }
}
