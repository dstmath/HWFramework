package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2FindNext2 extends SmbComTransaction {
    private String filename;
    private int flags;
    private int informationLevel;
    private int resumeKey;
    private int sid;

    Trans2FindNext2(int sid, int resumeKey, String filename) {
        this.sid = sid;
        this.resumeKey = resumeKey;
        this.filename = filename;
        this.command = (byte) 50;
        this.subCommand = (byte) 2;
        this.informationLevel = 260;
        this.flags = 0;
        this.maxParameterCount = 8;
        this.maxDataCount = Trans2FindFirst2.LIST_SIZE;
        this.maxSetupCount = (byte) 0;
    }

    void reset(int resumeKey, String lastName) {
        super.reset();
        this.resumeKey = resumeKey;
        this.filename = lastName;
        this.flags2 = 0;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + 1;
        dst[i] = (byte) 0;
        return 2;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.sid, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) Trans2FindFirst2.LIST_COUNT, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.informationLevel, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.resumeKey, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
        dstIndex += 2;
        return (dstIndex + writeString(this.filename, dst, dstIndex)) - start;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("Trans2FindNext2[" + super.toString() + ",sid=" + this.sid + ",searchCount=" + Trans2FindFirst2.LIST_SIZE + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + ",resumeKey=0x" + Hexdump.toHexString(this.resumeKey, 4) + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",filename=" + this.filename + "]");
    }
}
