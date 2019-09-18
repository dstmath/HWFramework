package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2FindNext2 extends SmbComTransaction {
    private String filename;
    private int flags = 0;
    private int informationLevel = 260;
    private int resumeKey;
    private int sid;

    Trans2FindNext2(int sid2, int resumeKey2, String filename2) {
        this.sid = sid2;
        this.resumeKey = resumeKey2;
        this.filename = filename2;
        this.command = 50;
        this.subCommand = 2;
        this.maxParameterCount = 8;
        this.maxDataCount = Trans2FindFirst2.LIST_SIZE;
        this.maxSetupCount = 0;
    }

    /* access modifiers changed from: package-private */
    public void reset(int resumeKey2, String lastName) {
        super.reset();
        this.resumeKey = resumeKey2;
        this.filename = lastName;
        this.flags2 = 0;
    }

    /* access modifiers changed from: package-private */
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        int i = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        return 2;
    }

    /* access modifiers changed from: package-private */
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        writeInt2((long) this.sid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt2((long) Trans2FindFirst2.LIST_COUNT, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        writeInt2((long) this.informationLevel, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        writeInt4((long) this.resumeKey, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 4;
        writeInt2((long) this.flags, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 2;
        return (dstIndex6 + writeString(this.filename, dst, dstIndex6)) - start;
    }

    /* access modifiers changed from: package-private */
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("Trans2FindNext2[" + super.toString() + ",sid=" + this.sid + ",searchCount=" + Trans2FindFirst2.LIST_SIZE + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + ",resumeKey=0x" + Hexdump.toHexString(this.resumeKey, 4) + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",filename=" + this.filename + "]");
    }
}
