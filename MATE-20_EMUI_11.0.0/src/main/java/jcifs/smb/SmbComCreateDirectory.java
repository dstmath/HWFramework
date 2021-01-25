package jcifs.smb;

/* access modifiers changed from: package-private */
public class SmbComCreateDirectory extends ServerMessageBlock {
    SmbComCreateDirectory(String directoryName) {
        this.path = directoryName;
        this.command = 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = 4;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - dstIndex;
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
        return new String("SmbComCreateDirectory[" + super.toString() + ",directoryName=" + this.path + "]");
    }
}
