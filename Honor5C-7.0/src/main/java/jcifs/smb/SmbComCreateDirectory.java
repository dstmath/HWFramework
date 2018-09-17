package jcifs.smb;

class SmbComCreateDirectory extends ServerMessageBlock {
    SmbComCreateDirectory(String directoryName) {
        this.path = directoryName;
        this.command = (byte) 0;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 4;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComCreateDirectory[" + super.toString() + ",directoryName=" + this.path + "]");
    }
}
