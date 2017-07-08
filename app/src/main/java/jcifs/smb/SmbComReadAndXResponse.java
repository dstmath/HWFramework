package jcifs.smb;

class SmbComReadAndXResponse extends AndXServerMessageBlock {
    byte[] b;
    int dataCompactionMode;
    int dataLength;
    int dataOffset;
    int off;

    SmbComReadAndXResponse() {
    }

    SmbComReadAndXResponse(byte[] b, int off) {
        this.b = b;
        this.off = off;
    }

    void setParam(byte[] b, int off) {
        this.b = b;
        this.off = off;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        bufferIndex += 2;
        this.dataCompactionMode = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 4;
        this.dataLength = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.dataOffset = ServerMessageBlock.readInt2(buffer, bufferIndex);
        return (bufferIndex + 12) - start;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComReadAndXResponse[" + super.toString() + ",dataCompactionMode=" + this.dataCompactionMode + ",dataLength=" + this.dataLength + ",dataOffset=" + this.dataOffset + "]");
    }
}
