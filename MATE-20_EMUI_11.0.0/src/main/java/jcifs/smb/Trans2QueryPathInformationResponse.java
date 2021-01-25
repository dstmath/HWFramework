package jcifs.smb;

import java.util.Date;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class Trans2QueryPathInformationResponse extends SmbComTransactionResponse {
    static final int SMB_QUERY_FILE_BASIC_INFO = 257;
    static final int SMB_QUERY_FILE_STANDARD_INFO = 258;
    Info info;
    private int informationLevel;

    /* access modifiers changed from: package-private */
    public class SmbQueryFileBasicInfo implements Info {
        int attributes;
        long changeTime;
        long createTime;
        long lastAccessTime;
        long lastWriteTime;

        SmbQueryFileBasicInfo() {
        }

        @Override // jcifs.smb.Info
        public int getAttributes() {
            return this.attributes;
        }

        @Override // jcifs.smb.Info
        public long getCreateTime() {
            return this.createTime;
        }

        @Override // jcifs.smb.Info
        public long getLastWriteTime() {
            return this.lastWriteTime;
        }

        @Override // jcifs.smb.Info
        public long getSize() {
            return 0;
        }

        public String toString() {
            return new String("SmbQueryFileBasicInfo[createTime=" + new Date(this.createTime) + ",lastAccessTime=" + new Date(this.lastAccessTime) + ",lastWriteTime=" + new Date(this.lastWriteTime) + ",changeTime=" + new Date(this.changeTime) + ",attributes=0x" + Hexdump.toHexString(this.attributes, 4) + "]");
        }
    }

    /* access modifiers changed from: package-private */
    public class SmbQueryFileStandardInfo implements Info {
        long allocationSize;
        boolean deletePending;
        boolean directory;
        long endOfFile;
        int numberOfLinks;

        SmbQueryFileStandardInfo() {
        }

        @Override // jcifs.smb.Info
        public int getAttributes() {
            return 0;
        }

        @Override // jcifs.smb.Info
        public long getCreateTime() {
            return 0;
        }

        @Override // jcifs.smb.Info
        public long getLastWriteTime() {
            return 0;
        }

        @Override // jcifs.smb.Info
        public long getSize() {
            return this.endOfFile;
        }

        public String toString() {
            return new String("SmbQueryInfoStandard[allocationSize=" + this.allocationSize + ",endOfFile=" + this.endOfFile + ",numberOfLinks=" + this.numberOfLinks + ",deletePending=" + this.deletePending + ",directory=" + this.directory + "]");
        }
    }

    Trans2QueryPathInformationResponse(int informationLevel2) {
        this.informationLevel = informationLevel2;
        this.subCommand = 5;
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
        return 2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        switch (this.informationLevel) {
            case SMB_QUERY_FILE_BASIC_INFO /* 257 */:
                return readSmbQueryFileBasicInfoWireFormat(buffer, bufferIndex);
            case SMB_QUERY_FILE_STANDARD_INFO /* 258 */:
                return readSmbQueryFileStandardInfoWireFormat(buffer, bufferIndex);
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int readSmbQueryFileStandardInfoWireFormat(byte[] buffer, int bufferIndex) {
        boolean z;
        boolean z2 = true;
        SmbQueryFileStandardInfo info2 = new SmbQueryFileStandardInfo();
        info2.allocationSize = readInt8(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 8;
        info2.endOfFile = readInt8(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 8;
        info2.numberOfLinks = readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        int bufferIndex5 = bufferIndex4 + 1;
        if ((buffer[bufferIndex4] & 255) > 0) {
            z = true;
        } else {
            z = false;
        }
        info2.deletePending = z;
        int bufferIndex6 = bufferIndex5 + 1;
        if ((buffer[bufferIndex5] & 255) <= 0) {
            z2 = false;
        }
        info2.directory = z2;
        this.info = info2;
        return bufferIndex6 - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    public int readSmbQueryFileBasicInfoWireFormat(byte[] buffer, int bufferIndex) {
        SmbQueryFileBasicInfo info2 = new SmbQueryFileBasicInfo();
        info2.createTime = readTime(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 8;
        info2.lastAccessTime = readTime(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 8;
        info2.lastWriteTime = readTime(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 8;
        info2.changeTime = readTime(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 8;
        info2.attributes = readInt2(buffer, bufferIndex5);
        this.info = info2;
        return (bufferIndex5 + 2) - bufferIndex;
    }

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("Trans2QueryPathInformationResponse[" + super.toString() + "]");
    }
}
