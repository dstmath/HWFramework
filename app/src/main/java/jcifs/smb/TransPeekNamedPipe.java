package jcifs.smb;

class TransPeekNamedPipe extends SmbComTransaction {
    private int fid;

    TransPeekNamedPipe(String pipeName, int fid) {
        this.name = pipeName;
        this.fid = fid;
        this.command = (byte) 37;
        this.subCommand = (byte) 35;
        this.timeout = -1;
        this.maxParameterCount = 6;
        this.maxDataCount = 1;
        this.maxSetupCount = (byte) 0;
        this.setupCount = 2;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + 1;
        dst[i] = (byte) 0;
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        return 4;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("TransPeekNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
