package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2QueryFSInformation extends SmbComTransaction {
    private int informationLevel;

    Trans2QueryFSInformation(int informationLevel2) {
        this.command = 50;
        this.subCommand = 3;
        this.informationLevel = informationLevel2;
        this.totalParameterCount = 2;
        this.totalDataCount = 0;
        this.maxParameterCount = 0;
        this.maxDataCount = 800;
        this.maxSetupCount = 0;
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
        writeInt2((long) this.informationLevel, dst, dstIndex);
        return (dstIndex + 2) - start;
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
        return new String("Trans2QueryFSInformation[" + super.toString() + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + "]");
    }
}
