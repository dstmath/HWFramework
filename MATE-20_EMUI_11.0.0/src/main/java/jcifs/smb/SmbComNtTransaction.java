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

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransaction, jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        int dstIndex3;
        if (this.command != -95) {
            dst[dstIndex] = this.maxSetupCount;
            dstIndex2 = dstIndex + 1;
        } else {
            dst[dstIndex] = 0;
            dstIndex2 = dstIndex + 1;
        }
        int dstIndex4 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex5 = dstIndex4 + 1;
        dst[dstIndex4] = 0;
        writeInt4((long) this.totalParameterCount, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        writeInt4((long) this.totalDataCount, dst, dstIndex6);
        int dstIndex7 = dstIndex6 + 4;
        if (this.command != -95) {
            writeInt4((long) this.maxParameterCount, dst, dstIndex7);
            int dstIndex8 = dstIndex7 + 4;
            writeInt4((long) this.maxDataCount, dst, dstIndex8);
            dstIndex7 = dstIndex8 + 4;
        }
        writeInt4((long) this.parameterCount, dst, dstIndex7);
        int dstIndex9 = dstIndex7 + 4;
        writeInt4((long) (this.parameterCount == 0 ? 0 : this.parameterOffset), dst, dstIndex9);
        int dstIndex10 = dstIndex9 + 4;
        if (this.command == -95) {
            writeInt4((long) this.parameterDisplacement, dst, dstIndex10);
            dstIndex10 += 4;
        }
        writeInt4((long) this.dataCount, dst, dstIndex10);
        int dstIndex11 = dstIndex10 + 4;
        writeInt4((long) (this.dataCount == 0 ? 0 : this.dataOffset), dst, dstIndex11);
        int dstIndex12 = dstIndex11 + 4;
        if (this.command == -95) {
            writeInt4((long) this.dataDisplacement, dst, dstIndex12);
            int dstIndex13 = dstIndex12 + 4;
            dst[dstIndex13] = 0;
            dstIndex3 = dstIndex13 + 1;
        } else {
            int dstIndex14 = dstIndex12 + 1;
            dst[dstIndex12] = (byte) this.setupCount;
            writeInt2((long) this.function, dst, dstIndex14);
            int dstIndex15 = dstIndex14 + 2;
            dstIndex3 = dstIndex15 + writeSetupWireFormat(dst, dstIndex15);
        }
        return dstIndex3 - dstIndex;
    }
}
