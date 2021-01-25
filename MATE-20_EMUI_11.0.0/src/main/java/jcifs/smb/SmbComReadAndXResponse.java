package jcifs.smb;

/* access modifiers changed from: package-private */
public class SmbComReadAndXResponse extends AndXServerMessageBlock {
    byte[] b;
    int dataCompactionMode;
    int dataLength;
    int dataOffset;
    int off;

    SmbComReadAndXResponse() {
    }

    SmbComReadAndXResponse(byte[] b2, int off2) {
        this.b = b2;
        this.off = off2;
    }

    /* access modifiers changed from: package-private */
    public void setParam(byte[] b2, int off2) {
        this.b = b2;
        this.off = off2;
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
        int bufferIndex2 = bufferIndex + 2;
        this.dataCompactionMode = readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 4;
        this.dataLength = readInt2(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 2;
        this.dataOffset = readInt2(buffer, bufferIndex4);
        return (bufferIndex4 + 12) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.AndXServerMessageBlock, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComReadAndXResponse[" + super.toString() + ",dataCompactionMode=" + this.dataCompactionMode + ",dataLength=" + this.dataLength + ",dataOffset=" + this.dataOffset + "]");
    }
}
