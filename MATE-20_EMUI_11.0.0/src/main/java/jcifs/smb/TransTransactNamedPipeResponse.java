package jcifs.smb;

/* access modifiers changed from: package-private */
public class TransTransactNamedPipeResponse extends SmbComTransactionResponse {
    private SmbNamedPipe pipe;

    TransTransactNamedPipeResponse(SmbNamedPipe pipe2) {
        this.pipe = pipe2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
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

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("TransTransactNamedPipeResponse[" + super.toString() + "]");
    }
}
