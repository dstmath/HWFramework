package jcifs.smb;

class Trans2SetFileInformation extends SmbComTransaction {
    static final int SMB_FILE_BASIC_INFO = 257;
    private int attributes;
    private long createTime;
    private int fid;
    private long lastWriteTime;

    Trans2SetFileInformation(int fid2, int attributes2, long createTime2, long lastWriteTime2) {
        this.fid = fid2;
        this.attributes = attributes2;
        this.createTime = createTime2;
        this.lastWriteTime = lastWriteTime2;
        this.command = 50;
        this.subCommand = 8;
        this.maxParameterCount = 6;
        this.maxDataCount = 0;
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
        writeInt2((long) this.fid, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt2(257, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        writeInt2(0, dst, dstIndex3);
        return (dstIndex3 + 2) - start;
    }

    /* access modifiers changed from: package-private */
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        writeTime(this.createTime, dst, dstIndex);
        int dstIndex2 = dstIndex + 8;
        writeInt8(0, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 8;
        writeTime(this.lastWriteTime, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 8;
        writeInt8(0, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 8;
        writeInt2((long) (this.attributes | 128), dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 2;
        writeInt8(0, dst, dstIndex6);
        return (dstIndex6 + 6) - start;
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
        return new String("Trans2SetFileInformation[" + super.toString() + ",fid=" + this.fid + "]");
    }
}
