package jcifs.smb;

/* access modifiers changed from: package-private */
public class SmbComClose extends ServerMessageBlock {
    private int fid;
    private long lastWriteTime;

    SmbComClose(int fid2, long lastWriteTime2) {
        this.fid = fid2;
        this.lastWriteTime = lastWriteTime2;
        this.command = 4;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.fid, dst, dstIndex);
        writeUTime(this.lastWriteTime, dst, dstIndex + 2);
        return 6;
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

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComClose[" + super.toString() + ",fid=" + this.fid + ",lastWriteTime=" + this.lastWriteTime + "]");
    }
}
