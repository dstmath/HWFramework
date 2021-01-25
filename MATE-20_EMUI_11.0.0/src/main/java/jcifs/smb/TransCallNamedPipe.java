package jcifs.smb;

import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public class TransCallNamedPipe extends SmbComTransaction {
    private byte[] pipeData;
    private int pipeDataLen;
    private int pipeDataOff;

    TransCallNamedPipe(String pipeName, byte[] data, int off, int len) {
        this.name = pipeName;
        this.pipeData = data;
        this.pipeDataOff = off;
        this.pipeDataLen = len;
        this.command = 37;
        this.subCommand = 84;
        this.timeout = -1;
        this.maxParameterCount = 0;
        this.maxDataCount = 65535;
        this.maxSetupCount = 0;
        this.setupCount = 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = 0;
        int i = dstIndex4 + 1;
        dst[dstIndex4] = 0;
        return 4;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
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

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    @Override // jcifs.smb.SmbComTransaction, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("TransCallNamedPipe[" + super.toString() + ",pipeName=" + this.name + "]");
    }
}
