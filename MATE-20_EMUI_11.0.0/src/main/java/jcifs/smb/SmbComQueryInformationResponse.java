package jcifs.smb;

import java.util.Date;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComQueryInformationResponse extends ServerMessageBlock implements Info {
    private int fileAttributes = 0;
    private int fileSize = 0;
    private long lastWriteTime = 0;
    private long serverTimeZoneOffset;

    SmbComQueryInformationResponse(long serverTimeZoneOffset2) {
        this.serverTimeZoneOffset = serverTimeZoneOffset2;
        this.command = 8;
    }

    @Override // jcifs.smb.Info
    public int getAttributes() {
        return this.fileAttributes;
    }

    @Override // jcifs.smb.Info
    public long getCreateTime() {
        return this.lastWriteTime + this.serverTimeZoneOffset;
    }

    @Override // jcifs.smb.Info
    public long getLastWriteTime() {
        return this.lastWriteTime + this.serverTimeZoneOffset;
    }

    @Override // jcifs.smb.Info
    public long getSize() {
        return (long) this.fileSize;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        if (this.wordCount == 0) {
            return 0;
        }
        this.fileAttributes = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        this.lastWriteTime = readUTime(buffer, bufferIndex2);
        this.fileSize = readInt4(buffer, bufferIndex2 + 4);
        return 20;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComQueryInformationResponse[" + super.toString() + ",fileAttributes=0x" + Hexdump.toHexString(this.fileAttributes, 4) + ",lastWriteTime=" + new Date(this.lastWriteTime) + ",fileSize=" + this.fileSize + "]");
    }
}
