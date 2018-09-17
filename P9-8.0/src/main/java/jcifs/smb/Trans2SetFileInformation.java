package jcifs.smb;

class Trans2SetFileInformation extends SmbComTransaction {
    static final int SMB_FILE_BASIC_INFO = 257;
    private int attributes;
    private long createTime;
    private int fid;
    private long lastWriteTime;

    Trans2SetFileInformation(int fid, int attributes, long createTime, long lastWriteTime) {
        this.fid = fid;
        this.attributes = attributes;
        this.createTime = createTime;
        this.lastWriteTime = lastWriteTime;
        this.command = (byte) 50;
        this.subCommand = (byte) 8;
        this.maxParameterCount = 6;
        this.maxDataCount = 0;
        this.maxSetupCount = (byte) 0;
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
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2(257, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2(0, dst, dstIndex);
        return (dstIndex + 2) - start;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeTime(this.createTime, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeInt8(0, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeTime(this.lastWriteTime, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeInt8(0, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeInt2((long) (this.attributes | 128), dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt8(0, dst, dstIndex);
        return (dstIndex + 6) - start;
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
        return new String("Trans2SetFileInformation[" + super.toString() + ",fid=" + this.fid + "]");
    }
}
