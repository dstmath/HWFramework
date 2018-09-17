package jcifs.smb;

abstract class SmbComNtTransaction extends SmbComTransaction {
    private static final int NTT_PRIMARY_SETUP_OFFSET = 69;
    private static final int NTT_SECONDARY_PARAMETER_OFFSET = 51;
    static final int NT_TRANSACT_QUERY_SECURITY_DESC = 6;
    int function;

    SmbComNtTransaction() {
        this.primarySetupOffset = NTT_PRIMARY_SETUP_OFFSET;
        this.secondaryParameterOffset = NTT_SECONDARY_PARAMETER_OFFSET;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        int start = dstIndex;
        if (this.command != (byte) -95) {
            dstIndex2 = dstIndex + 1;
            dst[dstIndex] = this.maxSetupCount;
            dstIndex = dstIndex2;
        } else {
            dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
        }
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
        ServerMessageBlock.writeInt4((long) this.totalParameterCount, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.totalDataCount, dst, dstIndex);
        dstIndex += 4;
        if (this.command != (byte) -95) {
            ServerMessageBlock.writeInt4((long) this.maxParameterCount, dst, dstIndex);
            dstIndex += 4;
            ServerMessageBlock.writeInt4((long) this.maxDataCount, dst, dstIndex);
            dstIndex += 4;
        }
        ServerMessageBlock.writeInt4((long) this.parameterCount, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) (this.parameterCount == 0 ? 0 : this.parameterOffset), dst, dstIndex);
        dstIndex += 4;
        if (this.command == (byte) -95) {
            ServerMessageBlock.writeInt4((long) this.parameterDisplacement, dst, dstIndex);
            dstIndex += 4;
        }
        ServerMessageBlock.writeInt4((long) this.dataCount, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) (this.dataCount == 0 ? 0 : this.dataOffset), dst, dstIndex);
        dstIndex += 4;
        if (this.command == (byte) -95) {
            ServerMessageBlock.writeInt4((long) this.dataDisplacement, dst, dstIndex);
            dstIndex += 4;
            dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
        } else {
            dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) this.setupCount;
            ServerMessageBlock.writeInt2((long) this.function, dst, dstIndex2);
            dstIndex = dstIndex2 + 2;
            dstIndex += writeSetupWireFormat(dst, dstIndex);
        }
        return dstIndex - start;
    }
}
