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
        this.command = (byte) 37;
        this.subCommand = (byte) 38;
        this.maxParameterCount = 0;
        this.maxDataCount = 65535;
        this.maxSetupCount = (byte) 0;
        this.setupCount = 2;
        this.name = "\\PIPE\\";
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + 1;
        dst[i] = (byte) 0;
        ServerMessageBlock.writeInt2((long) this.pipeFid, dst, dstIndex);
        dstIndex += 2;
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
                log.println("TransTransactNamedPipe data too long for buffer");
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
        return new String("TransTransactNamedPipe[" + super.toString() + ",pipeFid=" + this.pipeFid + "]");
    }
}
