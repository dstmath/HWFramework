package jcifs.smb;

/* access modifiers changed from: package-private */
public class SmbComOpenAndXResponse extends AndXServerMessageBlock {
    int action;
    int dataSize;
    int deviceState;
    int fid;
    int fileAttributes;
    int fileType;
    int grantedAccess;
    long lastWriteTime;
    int serverFid;

    SmbComOpenAndXResponse() {
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        this.fid = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        this.fileAttributes = readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 2;
        this.lastWriteTime = readUTime(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        this.dataSize = readInt4(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 4;
        this.grantedAccess = readInt2(buffer, bufferIndex5);
        int bufferIndex6 = bufferIndex5 + 2;
        this.fileType = readInt2(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 2;
        this.deviceState = readInt2(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 2;
        this.action = readInt2(buffer, bufferIndex8);
        int bufferIndex9 = bufferIndex8 + 2;
        this.serverFid = readInt4(buffer, bufferIndex9);
        return (bufferIndex9 + 6) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.AndXServerMessageBlock, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComOpenAndXResponse[" + super.toString() + ",fid=" + this.fid + ",fileAttributes=" + this.fileAttributes + ",lastWriteTime=" + this.lastWriteTime + ",dataSize=" + this.dataSize + ",grantedAccess=" + this.grantedAccess + ",fileType=" + this.fileType + ",deviceState=" + this.deviceState + ",action=" + this.action + ",serverFid=" + this.serverFid + "]");
    }
}
