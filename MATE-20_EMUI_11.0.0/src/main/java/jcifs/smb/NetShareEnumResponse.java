package jcifs.smb;

import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public class NetShareEnumResponse extends SmbComTransactionResponse {
    private int converter;
    private int totalAvailableEntries;

    NetShareEnumResponse() {
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
        this.status = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        this.converter = readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 2;
        this.numEntries = readInt2(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 2;
        this.totalAvailableEntries = readInt2(buffer, bufferIndex4);
        return (bufferIndex4 + 2) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        this.useUnicode = false;
        this.results = new SmbShareInfo[this.numEntries];
        for (int i = 0; i < this.numEntries; i++) {
            FileEntry[] fileEntryArr = this.results;
            SmbShareInfo e = new SmbShareInfo();
            fileEntryArr[i] = e;
            e.netName = readString(buffer, bufferIndex, 13, false);
            int bufferIndex2 = bufferIndex + 14;
            e.type = readInt2(buffer, bufferIndex2);
            int bufferIndex3 = bufferIndex2 + 2;
            int off = readInt4(buffer, bufferIndex3);
            bufferIndex = bufferIndex3 + 4;
            e.remark = readString(buffer, ((65535 & off) - this.converter) + bufferIndex, 128, false);
            LogStream logStream = log;
            if (LogStream.level >= 4) {
                log.println(e);
            }
        }
        return bufferIndex - bufferIndex;
    }

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("NetShareEnumResponse[" + super.toString() + ",status=" + this.status + ",converter=" + this.converter + ",entriesReturned=" + this.numEntries + ",totalAvailableEntries=" + this.totalAvailableEntries + "]");
    }
}
