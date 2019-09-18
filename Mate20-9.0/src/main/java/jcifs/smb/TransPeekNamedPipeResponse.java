package jcifs.smb;

class TransPeekNamedPipeResponse extends SmbComTransactionResponse {
    static final int STATUS_CONNECTION_OK = 3;
    static final int STATUS_DISCONNECTED = 1;
    static final int STATUS_LISTENING = 2;
    static final int STATUS_SERVER_END_CLOSED = 4;
    int available;
    private int head;
    private SmbNamedPipe pipe;
    int status;

    TransPeekNamedPipeResponse(SmbNamedPipe pipe2) {
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
        this.available = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        this.head = readInt2(buffer, bufferIndex2);
        this.status = readInt2(buffer, bufferIndex2 + 2);
        return 6;
    }

    /* access modifiers changed from: package-private */
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("TransPeekNamedPipeResponse[" + super.toString() + "]");
    }
}
