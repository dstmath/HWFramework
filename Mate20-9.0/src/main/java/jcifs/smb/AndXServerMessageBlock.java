package jcifs.smb;

import jcifs.util.Hexdump;

abstract class AndXServerMessageBlock extends ServerMessageBlock {
    private static final int ANDX_COMMAND_OFFSET = 1;
    private static final int ANDX_OFFSET_OFFSET = 3;
    private static final int ANDX_RESERVED_OFFSET = 2;
    ServerMessageBlock andx = null;
    private byte andxCommand = -1;
    private int andxOffset = 0;

    AndXServerMessageBlock() {
    }

    AndXServerMessageBlock(ServerMessageBlock andx2) {
        if (andx2 != null) {
            this.andx = andx2;
            this.andxCommand = andx2.command;
        }
    }

    /* access modifiers changed from: package-private */
    public int getBatchLimit(byte command) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int encode(byte[] dst, int dstIndex) {
        this.headerStart = dstIndex;
        int start = dstIndex;
        int dstIndex2 = dstIndex + writeHeaderWireFormat(dst, dstIndex);
        this.length = (dstIndex2 + writeAndXWireFormat(dst, dstIndex2)) - start;
        if (this.digest != null) {
            this.digest.sign(dst, this.headerStart, this.length, this, this.response);
        }
        return this.length;
    }

    /* access modifiers changed from: package-private */
    public int decode(byte[] buffer, int bufferIndex) {
        this.headerStart = bufferIndex;
        int start = bufferIndex;
        int bufferIndex2 = bufferIndex + readHeaderWireFormat(buffer, bufferIndex);
        this.length = (bufferIndex2 + readAndXWireFormat(buffer, bufferIndex2)) - start;
        return this.length;
    }

    /* access modifiers changed from: package-private */
    public int writeAndXWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        int start = dstIndex;
        this.wordCount = writeParameterWordsWireFormat(dst, start + 3 + 2);
        this.wordCount += 4;
        int dstIndex3 = dstIndex + this.wordCount + 1;
        this.wordCount /= 2;
        dst[start] = (byte) (this.wordCount & 255);
        this.byteCount = writeBytesWireFormat(dst, dstIndex3 + 2);
        int dstIndex4 = dstIndex3 + 1;
        dst[dstIndex3] = (byte) (this.byteCount & 255);
        dst[dstIndex4] = (byte) ((this.byteCount >> 8) & 255);
        int dstIndex5 = dstIndex4 + 1 + this.byteCount;
        if (this.andx == null || !USE_BATCHING || this.batchLevel >= getBatchLimit(this.andx.command)) {
            this.andxCommand = -1;
            this.andx = null;
            dst[start + 1] = -1;
            dst[start + 2] = 0;
            dst[start + 3] = -34;
            dst[start + 3 + 1] = -34;
            return dstIndex5 - start;
        }
        this.andx.batchLevel = this.batchLevel + 1;
        dst[start + 1] = this.andxCommand;
        dst[start + 2] = 0;
        this.andxOffset = dstIndex5 - this.headerStart;
        writeInt2((long) this.andxOffset, dst, start + 3);
        this.andx.useUnicode = this.useUnicode;
        if (this.andx instanceof AndXServerMessageBlock) {
            this.andx.uid = this.uid;
            dstIndex2 = dstIndex5 + ((AndXServerMessageBlock) this.andx).writeAndXWireFormat(dst, dstIndex5);
        } else {
            int andxStart = dstIndex5;
            this.andx.wordCount = this.andx.writeParameterWordsWireFormat(dst, dstIndex5);
            int dstIndex6 = dstIndex5 + this.andx.wordCount + 1;
            this.andx.wordCount /= 2;
            dst[andxStart] = (byte) (this.andx.wordCount & 255);
            this.andx.byteCount = this.andx.writeBytesWireFormat(dst, dstIndex6 + 2);
            int dstIndex7 = dstIndex6 + 1;
            dst[dstIndex6] = (byte) (this.andx.byteCount & 255);
            dst[dstIndex7] = (byte) ((this.andx.byteCount >> 8) & 255);
            dstIndex2 = dstIndex7 + 1 + this.andx.byteCount;
        }
        return dstIndex2 - start;
    }

    /* access modifiers changed from: package-private */
    public int readAndXWireFormat(byte[] buffer, int bufferIndex) {
        int bufferIndex2;
        int bufferIndex3;
        int bufferIndex4;
        int start = bufferIndex;
        int bufferIndex5 = bufferIndex + 1;
        this.wordCount = buffer[bufferIndex];
        if (this.wordCount != 0) {
            this.andxCommand = buffer[bufferIndex5];
            this.andxOffset = readInt2(buffer, bufferIndex5 + 2);
            if (this.andxOffset == 0) {
                this.andxCommand = -1;
            }
            if (this.wordCount > 2) {
                readParameterWordsWireFormat(buffer, bufferIndex5 + 4);
                if (this.command == -94 && ((SmbComNTCreateAndXResponse) this).isExtended) {
                    this.wordCount += 8;
                }
            }
            bufferIndex2 = start + 1 + (this.wordCount * 2);
        } else {
            bufferIndex2 = bufferIndex5;
        }
        this.byteCount = readInt2(buffer, bufferIndex2);
        int bufferIndex6 = bufferIndex2 + 2;
        if (this.byteCount != 0) {
            int readBytesWireFormat = readBytesWireFormat(buffer, bufferIndex6);
            bufferIndex6 += this.byteCount;
        }
        if (this.errorCode != 0 || this.andxCommand == -1) {
            this.andxCommand = -1;
            this.andx = null;
        } else if (this.andx == null) {
            this.andxCommand = -1;
            throw new RuntimeException("no andx command supplied with response");
        } else {
            int bufferIndex7 = this.headerStart + this.andxOffset;
            this.andx.headerStart = this.headerStart;
            this.andx.command = this.andxCommand;
            this.andx.errorCode = this.errorCode;
            this.andx.flags = this.flags;
            this.andx.flags2 = this.flags2;
            this.andx.tid = this.tid;
            this.andx.pid = this.pid;
            this.andx.uid = this.uid;
            this.andx.mid = this.mid;
            this.andx.useUnicode = this.useUnicode;
            if (this.andx instanceof AndXServerMessageBlock) {
                bufferIndex3 = bufferIndex7 + ((AndXServerMessageBlock) this.andx).readAndXWireFormat(buffer, bufferIndex7);
            } else {
                int bufferIndex8 = bufferIndex7 + 1;
                buffer[bufferIndex7] = (byte) (this.andx.wordCount & 255);
                if (this.andx.wordCount == 0 || this.andx.wordCount <= 2) {
                    bufferIndex4 = bufferIndex8;
                } else {
                    bufferIndex4 = bufferIndex8 + this.andx.readParameterWordsWireFormat(buffer, bufferIndex8);
                }
                this.andx.byteCount = readInt2(buffer, bufferIndex4);
                bufferIndex3 = bufferIndex4 + 2;
                if (this.andx.byteCount != 0) {
                    this.andx.readBytesWireFormat(buffer, bufferIndex3);
                    bufferIndex3 += this.andx.byteCount;
                }
            }
            this.andx.received = true;
        }
        return bufferIndex3 - start;
    }

    public String toString() {
        return new String(super.toString() + ",andxCommand=0x" + Hexdump.toHexString((int) this.andxCommand, 2) + ",andxOffset=" + this.andxOffset);
    }
}
