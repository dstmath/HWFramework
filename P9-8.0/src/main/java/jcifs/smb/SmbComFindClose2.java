package jcifs.smb;

class SmbComFindClose2 extends ServerMessageBlock {
    private int sid;

    SmbComFindClose2(int sid) {
        this.sid = sid;
        this.command = (byte) 52;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        ServerMessageBlock.writeInt2((long) this.sid, dst, dstIndex);
        return 2;
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
        return new String("SmbComFindClose2[" + super.toString() + ",sid=" + this.sid + "]");
    }
}
