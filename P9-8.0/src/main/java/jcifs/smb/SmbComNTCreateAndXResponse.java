package jcifs.smb;

import java.util.Date;
import jcifs.util.Hexdump;

class SmbComNTCreateAndXResponse extends AndXServerMessageBlock {
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

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        int bufferIndex2 = bufferIndex + 1;
        this.oplockLevel = buffer[bufferIndex];
        this.fid = ServerMessageBlock.readInt2(buffer, bufferIndex2);
        bufferIndex = bufferIndex2 + 2;
        this.createAction = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.creationTime = ServerMessageBlock.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.lastAccessTime = ServerMessageBlock.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.lastWriteTime = ServerMessageBlock.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.changeTime = ServerMessageBlock.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.extFileAttributes = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.allocationSize = ServerMessageBlock.readInt8(buffer, bufferIndex);
        bufferIndex += 8;
        this.endOfFile = ServerMessageBlock.readInt8(buffer, bufferIndex);
        bufferIndex += 8;
        this.fileType = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.deviceState = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        bufferIndex2 = bufferIndex + 1;
        this.directory = (buffer[bufferIndex] & 255) > 0;
        return bufferIndex2 - start;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComNTCreateAndXResponse[" + super.toString() + ",oplockLevel=" + this.oplockLevel + ",fid=" + this.fid + ",createAction=0x" + Hexdump.toHexString(this.createAction, 4) + ",creationTime=" + new Date(this.creationTime) + ",lastAccessTime=" + new Date(this.lastAccessTime) + ",lastWriteTime=" + new Date(this.lastWriteTime) + ",changeTime=" + new Date(this.changeTime) + ",extFileAttributes=0x" + Hexdump.toHexString(this.extFileAttributes, 4) + ",allocationSize=" + this.allocationSize + ",endOfFile=" + this.endOfFile + ",fileType=" + this.fileType + ",deviceState=" + this.deviceState + ",directory=" + this.directory + "]");
    }
}
