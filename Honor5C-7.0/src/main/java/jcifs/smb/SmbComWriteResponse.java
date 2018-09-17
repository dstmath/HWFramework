package jcifs.smb;

class SmbComWriteResponse extends ServerMessageBlock {
    long count;

    SmbComWriteResponse() {
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        this.count = ((long) ServerMessageBlock.readInt2(buffer, bufferIndex)) & 65535;
        return 8;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComWriteResponse[" + super.toString() + ",count=" + this.count + "]");
    }
}
