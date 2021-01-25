package jcifs.smb;

/* access modifiers changed from: package-private */
public class Trans2QueryFSInformationResponse extends SmbComTransactionResponse {
    static final int SMB_FS_FULL_SIZE_INFORMATION = 1007;
    static final int SMB_INFO_ALLOCATION = 1;
    static final int SMB_QUERY_FS_SIZE_INFO = 259;
    AllocInfo info;
    private int informationLevel;

    /* access modifiers changed from: package-private */
    public class SmbInfoAllocation implements AllocInfo {
        long alloc;
        int bytesPerSect;
        long free;
        int sectPerAlloc;

        SmbInfoAllocation() {
        }

        @Override // jcifs.smb.AllocInfo
        public long getCapacity() {
            return this.alloc * ((long) this.sectPerAlloc) * ((long) this.bytesPerSect);
        }

        @Override // jcifs.smb.AllocInfo
        public long getFree() {
            return this.free * ((long) this.sectPerAlloc) * ((long) this.bytesPerSect);
        }

        public String toString() {
            return new String("SmbInfoAllocation[alloc=" + this.alloc + ",free=" + this.free + ",sectPerAlloc=" + this.sectPerAlloc + ",bytesPerSect=" + this.bytesPerSect + "]");
        }
    }

    Trans2QueryFSInformationResponse(int informationLevel2) {
        this.informationLevel = informationLevel2;
        this.command = 50;
        this.subCommand = 3;
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
        switch (this.informationLevel) {
            case 1:
                return readSmbInfoAllocationWireFormat(buffer, bufferIndex);
            case SMB_QUERY_FS_SIZE_INFO /* 259 */:
                return readSmbQueryFSSizeInfoWireFormat(buffer, bufferIndex);
            case SMB_FS_FULL_SIZE_INFORMATION /* 1007 */:
                return readFsFullSizeInformationWireFormat(buffer, bufferIndex);
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int readSmbInfoAllocationWireFormat(byte[] buffer, int bufferIndex) {
        SmbInfoAllocation info2 = new SmbInfoAllocation();
        int bufferIndex2 = bufferIndex + 4;
        info2.sectPerAlloc = readInt4(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 4;
        info2.alloc = (long) readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        info2.free = (long) readInt4(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 4;
        info2.bytesPerSect = readInt2(buffer, bufferIndex5);
        this.info = info2;
        return (bufferIndex5 + 4) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    public int readSmbQueryFSSizeInfoWireFormat(byte[] buffer, int bufferIndex) {
        SmbInfoAllocation info2 = new SmbInfoAllocation();
        info2.alloc = readInt8(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 8;
        info2.free = readInt8(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 8;
        info2.sectPerAlloc = readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        info2.bytesPerSect = readInt4(buffer, bufferIndex4);
        this.info = info2;
        return (bufferIndex4 + 4) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    public int readFsFullSizeInformationWireFormat(byte[] buffer, int bufferIndex) {
        SmbInfoAllocation info2 = new SmbInfoAllocation();
        info2.alloc = readInt8(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 8;
        info2.free = readInt8(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 8 + 8;
        info2.sectPerAlloc = readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        info2.bytesPerSect = readInt4(buffer, bufferIndex4);
        this.info = info2;
        return (bufferIndex4 + 4) - bufferIndex;
    }

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("Trans2QueryFSInformationResponse[" + super.toString() + "]");
    }
}
