package jcifs.smb;

class SmbComLogoffAndX extends AndXServerMessageBlock {
    SmbComLogoffAndX(ServerMessageBlock andx) {
        super(andx);
        this.command = 116;
    }

    /* access modifiers changed from: package-private */
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComLogoffAndX[" + super.toString() + "]");
    }
}
