package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2QueryFSInformation extends SmbComTransaction {
    private int informationLevel;

    Trans2QueryFSInformation(int informationLevel) {
        this.command = (byte) 50;
        this.subCommand = (byte) 3;
        this.informationLevel = informationLevel;
        this.totalParameterCount = 2;
        this.totalDataCount = 0;
        this.maxParameterCount = 0;
        this.maxDataCount = 800;
        this.maxSetupCount = (byte) 0;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + 1;
        dst[i] = (byte) 0;
        return 2;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.informationLevel, dst, dstIndex);
        return (dstIndex + 2) - start;
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
        return new String("Trans2QueryFSInformation[" + super.toString() + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, 3) + "]");
    }
}
