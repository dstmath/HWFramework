package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2QueryPathInformation extends SmbComTransaction {
    private int informationLevel;

    Trans2QueryPathInformation(String filename, int informationLevel2) {
        this.path = filename;
        this.informationLevel = informationLevel2;
        this.command = 50;
        this.subCommand = 5;
        this.totalDataCount = 0;
        this.maxParameterCount = 2;
        this.maxDataCount = 40;
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
        int dstIndex2 = dstIndex + 2;
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = 0;
        int dstIndex5 = dstIndex4 + 1;
        dst[dstIndex4] = 0;
        int dstIndex6 = dstIndex5 + 1;
        dst[dstIndex5] = 0;
        return (dstIndex6 + writeString(this.path, dst, dstIndex6)) - start;
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
        return new String("Trans2QueryPathInformation[" + super.toString() + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + ",filename=" + this.path + "]");
    }
}
