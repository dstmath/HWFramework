package jcifs.smb;

/* access modifiers changed from: package-private */
public class SmbComFindClose2 extends ServerMessageBlock {
    private int sid;

    SmbComFindClose2(int sid2) {
        this.sid = sid2;
        this.command = 52;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.sid, dst, dstIndex);
        return 2;
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
        return new String("SmbComFindClose2[" + super.toString() + ",sid=" + this.sid + "]");
    }
}
