package jcifs.smb;

import jcifs.util.LogStream;

class TransTransactNamedPipe extends SmbComTransaction {
    private byte[] pipeData;
    private int pipeDataLen;
    private int pipeDataOff;
    private int pipeFid;

    TransTransactNamedPipe(int fid, byte[] data, int off, int len) {
        this.pipeFid = fid;
        this.pipeData = data;
        this.pipeDataOff = off;
        this.pipeDataLen = len;
        this.command = 37;
        this.subCommand = 38;
        this.maxParameterCount = 0;
        this.maxDataCount = 65535;
        this.maxSetupCount = 0;
        this.setupCount = 2;
        this.name = "\\PIPE\\";
    }

    /* access modifiers changed from: package-private */
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        writeInt2((long) this.pipeFid, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        return 4;
    }

    /* access modifiers changed from: package-private */
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        if (dst.length - dstIndex < this.pipeDataLen) {
            LogStream logStream = log;
            if (LogStream.level >= 3) {
                log.println("TransTransactNamedPipe data too long for buffer");
            }
            return 0;
        }
        System.arraycopy(this.pipeData, this.pipeDataOff, dst, dstIndex, this.pipeDataLen);
        return this.pipeDataLen;
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
        return new String("TransTransactNamedPipe[" + super.toString() + ",pipeFid=" + this.pipeFid + "]");
    }
}
