package jcifs.smb;

class SmbComLogoffAndX extends AndXServerMessageBlock {
    SmbComLogoffAndX(ServerMessageBlock andx) {
        super(andx);
        this.command = (byte) 116;
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
        return new String("SmbComLogoffAndX[" + super.toString() + "]");
    }
}
