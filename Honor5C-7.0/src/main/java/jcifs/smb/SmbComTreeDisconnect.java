package jcifs.smb;

class SmbComTreeDisconnect extends ServerMessageBlock {
    SmbComTreeDisconnect() {
        this.command = (byte) 113;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
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
        return new String("SmbComTreeDisconnect[" + super.toString() + "]");
    }
}
