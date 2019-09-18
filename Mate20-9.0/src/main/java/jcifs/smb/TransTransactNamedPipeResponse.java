package jcifs.smb;

class TransTransactNamedPipeResponse extends SmbComTransactionResponse {
    private SmbNamedPipe pipe;

    TransTransactNamedPipeResponse(SmbNamedPipe pipe2) {
        this.pipe = pipe2;
    }

    /* access modifiers changed from: package-private */
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
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
        if (this.pipe.pipeIn != null) {
            TransactNamedPipeInputStream in = (TransactNamedPipeInputStream) this.pipe.pipeIn;
            synchronized (in.lock) {
                in.receive(buffer, bufferIndex, len);
                in.lock.notify();
            }
        }
        return len;
    }

    public String toString() {
        return new String("TransTransactNamedPipeResponse[" + super.toString() + "]");
    }
}
