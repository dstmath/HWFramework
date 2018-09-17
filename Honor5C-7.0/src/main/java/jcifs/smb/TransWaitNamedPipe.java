package jcifs.smb;

class TransWaitNamedPipe extends SmbComTransaction {
    TransWaitNamedPipe(String pipeName) {
        this.name = pipeName;
        this.command = (byte) 37;
        this.subCommand = (byte) 83;
        this.timeout = -1;
        this.maxParameterCount = 0;
        this.maxDataCount = 0;
        this.maxSetupCount = (byte) 0;
        this.setupCount = 2;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
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
        return new String("TransWaitNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
