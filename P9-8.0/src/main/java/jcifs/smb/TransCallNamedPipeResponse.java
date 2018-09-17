package jcifs.smb;

class TransCallNamedPipeResponse extends SmbComTransactionResponse {
    private SmbNamedPipe pipe;

    TransCallNamedPipeResponse(SmbNamedPipe pipe) {
        this.pipe = pipe;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        if (this.pipe.pipeIn != null) {
            TransactNamedPipeInputStream in = this.pipe.pipeIn;
            synchronized (in.lock) {
                in.receive(buffer, bufferIndex, len);
                in.lock.notify();
            }
        }
        return len;
    }

    public String toString() {
        return new String("TransCallNamedPipeResponse[" + super.toString() + "]");
    }
}
