package jcifs.smb;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public class Trans2FindFirst2Response extends SmbComTransactionResponse {
    static final int SMB_FILE_BOTH_DIRECTORY_INFO = 260;
    static final int SMB_FILE_NAMES_INFO = 259;
    static final int SMB_FIND_FILE_DIRECTORY_INFO = 257;
    static final int SMB_FIND_FILE_FULL_DIRECTORY_INFO = 258;
    static final int SMB_INFO_QUERY_EAS_FROM_LIST = 3;
    static final int SMB_INFO_QUERY_EA_SIZE = 2;
    static final int SMB_INFO_STANDARD = 1;
    int eaErrorOffset;
    boolean isEndOfSearch;
    String lastName;
    int lastNameBufferIndex;
    int lastNameOffset;
    int resumeKey;
    int sid;

    class SmbFindFileBothDirectoryInfo implements FileEntry {
        long allocationSize;
        long changeTime;
        long creationTime;
        int eaSize;
        long endOfFile;
        int extFileAttributes;
        int fileIndex;
        int fileNameLength;
        String filename;
        long lastAccessTime;
        long lastWriteTime;
        int nextEntryOffset;
        String shortName;
        int shortNameLength;

        SmbFindFileBothDirectoryInfo() {
        }

        @Override // jcifs.smb.FileEntry
        public String getName() {
            return this.filename;
        }

        @Override // jcifs.smb.FileEntry
        public int getType() {
            return 1;
        }

        @Override // jcifs.smb.FileEntry
        public int getAttributes() {
            return this.extFileAttributes;
        }

        @Override // jcifs.smb.FileEntry
        public long createTime() {
            return this.creationTime;
        }

        @Override // jcifs.smb.FileEntry
        public long lastModified() {
            return this.lastWriteTime;
        }

        @Override // jcifs.smb.FileEntry
        public long length() {
            return this.endOfFile;
        }

        public String toString() {
            return new String("SmbFindFileBothDirectoryInfo[nextEntryOffset=" + this.nextEntryOffset + ",fileIndex=" + this.fileIndex + ",creationTime=" + new Date(this.creationTime) + ",lastAccessTime=" + new Date(this.lastAccessTime) + ",lastWriteTime=" + new Date(this.lastWriteTime) + ",changeTime=" + new Date(this.changeTime) + ",endOfFile=" + this.endOfFile + ",allocationSize=" + this.allocationSize + ",extFileAttributes=" + this.extFileAttributes + ",fileNameLength=" + this.fileNameLength + ",eaSize=" + this.eaSize + ",shortNameLength=" + this.shortNameLength + ",shortName=" + this.shortName + ",filename=" + this.filename + "]");
        }
    }

    Trans2FindFirst2Response() {
        this.command = 50;
        this.subCommand = 1;
    }

    /* access modifiers changed from: package-private */
    public String readString(byte[] src, int srcIndex, int len) {
        try {
            if (this.useUnicode) {
                return new String(src, srcIndex, len, SmbConstants.UNI_ENCODING);
            }
            if (len > 0 && src[(srcIndex + len) - 1] == 0) {
                len--;
            }
            return new String(src, srcIndex, len, ServerMessageBlock.OEM_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            LogStream logStream = log;
            if (LogStream.level <= 1) {
                return null;
            }
            uee.printStackTrace(log);
            return null;
        }
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
        boolean z = true;
        if (this.subCommand == 1) {
            this.sid = readInt2(buffer, bufferIndex);
            bufferIndex += 2;
        }
        this.numEntries = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        if ((buffer[bufferIndex2] & 1) != 1) {
            z = false;
        }
        this.isEndOfSearch = z;
        int bufferIndex3 = bufferIndex2 + 2;
        this.eaErrorOffset = readInt2(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 2;
        this.lastNameOffset = readInt2(buffer, bufferIndex4);
        return (bufferIndex4 + 2) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.SmbComTransactionResponse
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        this.lastNameBufferIndex = this.lastNameOffset + bufferIndex;
        this.results = new SmbFindFileBothDirectoryInfo[this.numEntries];
        for (int i = 0; i < this.numEntries; i++) {
            FileEntry[] fileEntryArr = this.results;
            SmbFindFileBothDirectoryInfo e = new SmbFindFileBothDirectoryInfo();
            fileEntryArr[i] = e;
            e.nextEntryOffset = readInt4(buffer, bufferIndex);
            e.fileIndex = readInt4(buffer, bufferIndex + 4);
            e.creationTime = readTime(buffer, bufferIndex + 8);
            e.lastWriteTime = readTime(buffer, bufferIndex + 24);
            e.endOfFile = readInt8(buffer, bufferIndex + 40);
            e.extFileAttributes = readInt4(buffer, bufferIndex + 56);
            e.fileNameLength = readInt4(buffer, bufferIndex + 60);
            e.filename = readString(buffer, bufferIndex + 94, e.fileNameLength);
            if (this.lastNameBufferIndex >= bufferIndex && (e.nextEntryOffset == 0 || this.lastNameBufferIndex < e.nextEntryOffset + bufferIndex)) {
                this.lastName = e.filename;
                this.resumeKey = e.fileIndex;
            }
            bufferIndex += e.nextEntryOffset;
        }
        return this.dataCount;
    }

    @Override // jcifs.smb.SmbComTransactionResponse, jcifs.smb.ServerMessageBlock
    public String toString() {
        String c;
        if (this.subCommand == 1) {
            c = "Trans2FindFirst2Response[";
        } else {
            c = "Trans2FindNext2Response[";
        }
        return new String(c + super.toString() + ",sid=" + this.sid + ",searchCount=" + this.numEntries + ",isEndOfSearch=" + this.isEndOfSearch + ",eaErrorOffset=" + this.eaErrorOffset + ",lastNameOffset=" + this.lastNameOffset + ",lastName=" + this.lastName + "]");
    }
}
