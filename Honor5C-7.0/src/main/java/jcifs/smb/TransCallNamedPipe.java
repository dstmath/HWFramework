package jcifs.smb;

import jcifs.util.LogStream;

class TransCallNamedPipe extends SmbComTransaction {
    private byte[] pipeData;
    private int pipeDataLen;
    private int pipeDataOff;

    TransCallNamedPipe(String pipeName, byte[] data, int off, int len) {
        this.name = pipeName;
        this.pipeData = data;
        this.pipeDataOff = off;
        this.pipeDataLen = len;
        this.command = (byte) 37;
        this.subCommand = (byte) 84;
        this.timeout = -1;
        this.maxParameterCount = 0;
        this.maxDataCount = 65535;
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
        if (dst.length - dstIndex < this.pipeDataLen) {
            LogStream logStream = log;
            if (LogStream.level >= 3) {
                log.println("TransCallNamedPipe data too long for buffer");
            }
            return 0;
        }
        System.arraycopy(this.pipeData, this.pipeDataOff, dst, dstIndex, this.pipeDataLen);
        return this.pipeDataLen;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("TransCallNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
