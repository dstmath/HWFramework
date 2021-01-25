package jcifs.smb;

import java.util.Date;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComNTCreateAndXResponse extends AndXServerMessageBlock {
    static final int BATCH_OPLOCK_GRANTED = 2;
    static final int EXCLUSIVE_OPLOCK_GRANTED = 1;
    static final int LEVEL_II_OPLOCK_GRANTED = 3;
    long allocationSize;
    long changeTime;
    int createAction;
    long creationTime;
    int deviceState;
    boolean directory;
    long endOfFile;
    int extFileAttributes;
    int fid;
    int fileType;
    boolean isExtended;
    long lastAccessTime;
    long lastWriteTime;
    byte oplockLevel;

    SmbComNTCreateAndXResponse() {
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
        int bufferIndex2 = bufferIndex + 1;
        this.oplockLevel = buffer[bufferIndex];
        this.fid = readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 2;
        this.createAction = readInt4(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 4;
        this.creationTime = readTime(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 8;
        this.lastAccessTime = readTime(buffer, bufferIndex5);
        int bufferIndex6 = bufferIndex5 + 8;
        this.lastWriteTime = readTime(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 8;
        this.changeTime = readTime(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 8;
        this.extFileAttributes = readInt4(buffer, bufferIndex8);
        int bufferIndex9 = bufferIndex8 + 4;
        this.allocationSize = readInt8(buffer, bufferIndex9);
        int bufferIndex10 = bufferIndex9 + 8;
        this.endOfFile = readInt8(buffer, bufferIndex10);
        int bufferIndex11 = bufferIndex10 + 8;
        this.fileType = readInt2(buffer, bufferIndex11);
        int bufferIndex12 = bufferIndex11 + 2;
        this.deviceState = readInt2(buffer, bufferIndex12);
        int bufferIndex13 = bufferIndex12 + 2;
        int bufferIndex14 = bufferIndex13 + 1;
        this.directory = (buffer[bufferIndex13] & 255) > 0;
        return bufferIndex14 - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.AndXServerMessageBlock, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComNTCreateAndXResponse[" + super.toString() + ",oplockLevel=" + ((int) this.oplockLevel) + ",fid=" + this.fid + ",createAction=0x" + Hexdump.toHexString(this.createAction, 4) + ",creationTime=" + new Date(this.creationTime) + ",lastAccessTime=" + new Date(this.lastAccessTime) + ",lastWriteTime=" + new Date(this.lastWriteTime) + ",changeTime=" + new Date(this.changeTime) + ",extFileAttributes=0x" + Hexdump.toHexString(this.extFileAttributes, 4) + ",allocationSize=" + this.allocationSize + ",endOfFile=" + this.endOfFile + ",fileType=" + this.fileType + ",deviceState=" + this.deviceState + ",directory=" + this.directory + "]");
    }
}
