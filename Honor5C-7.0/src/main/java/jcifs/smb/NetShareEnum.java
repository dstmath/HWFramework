package jcifs.smb;

import java.io.UnsupportedEncodingException;

class NetShareEnum extends SmbComTransaction {
    private static final String DESCR = "WrLeh\u0000B13BWz\u0000";

    NetShareEnum() {
        this.command = (byte) 37;
        this.subCommand = (byte) 0;
        this.name = new String("\\PIPE\\LANMAN");
        this.maxParameterCount = 8;
        this.maxSetupCount = (byte) 0;
        this.setupCount = 0;
        this.timeout = 5000;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        try {
            byte[] descr = DESCR.getBytes("ASCII");
            ServerMessageBlock.writeInt2(0, dst, dstIndex);
            dstIndex += 2;
            System.arraycopy(descr, 0, dst, dstIndex, descr.length);
            dstIndex += descr.length;
            ServerMessageBlock.writeInt2(1, dst, dstIndex);
            dstIndex += 2;
            ServerMessageBlock.writeInt2((long) this.maxDataCount, dst, dstIndex);
            return (dstIndex + 2) - start;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
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
        return 0;
    }

    public String toString() {
        return new String("NetShareEnum[" + super.toString() + "]");
    }
}
